package d3e.core;

import java.io.IOException;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.UnexpectedRollbackException;

import classes.ClassUtils;
import store.DataStoreEvent;
import store.DatabaseObject;

@Component
public class TransactionWrapper {

	@Autowired
	private D3ESubscription subscription;

	@Autowired
	private TransactionDeligate deligate;

	@Autowired
	private EntityManager entityManager;

	public void doInTransaction(TransactionDeligate.ToRun run) throws ServletException, IOException {
		boolean created = createTransactionManager();
		boolean success = false;
		try {
			deligate.run(run);
			if (created) {
				publishEvents();
			}
			success = true;
		} catch (UnexpectedRollbackException e) {
			D3ELogger.info("Transaction failed");
		} catch (Exception e) {
			D3ELogger.printStackTrace(e);
			throw new RuntimeException(e);
		} finally {
			if (created) {
				if (!success) {
					TransactionManager manager = TransactionManager.get();
					if (manager != null) {
						manager.clearChanges();
					}
				}
				TransactionManager.remove();
			}
		}
	}

	private void publishEvents() throws ServletException, IOException {
		deligate.readOnly(() -> {
			TransactionManager manager = TransactionManager.get();
			TransactionManager.remove();
			createTransactionManager();
			manager.commit((type, entity) -> {
				try {
					if (entity instanceof DatabaseObject) {
						DatabaseObject db = (DatabaseObject) entity;
						if (db._isEntity()) {
							entity = refresh(db);
							db.updateMasters(o -> {
								refresh(o);
							});
						}
					}
					DataStoreEvent event = new DataStoreEvent(entity);
					event.setType(type);
					subscription.handleContextStart(event);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			manager.clearChanges();
		});
		if (!TransactionManager.get().isEmpty()) {
			publishEvents();
		}
	}

	private DatabaseObject refresh(DatabaseObject obj) {
		if(!obj._isEntity()) {
			return obj;
		}
		DatabaseObject load = (DatabaseObject) entityManager.find(ClassUtils.getClass(obj), obj.getId());
		if(load == null) {
			return obj;
		}
		load.setOld(obj.getOld());
		load._updateChanges(obj);
		return load;
	}

	private boolean createTransactionManager() {
		TransactionManager manager = TransactionManager.get();
		if (manager == null) {
			manager = new TransactionManager();
			TransactionManager.set(manager);
			return true;
		}
		return false;
	}
}
