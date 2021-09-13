package rest;

import classes.AllCustomersWithAgedGuardians;
import classes.AllCustomersWithAgedGuardians2;
import classes.AllCustomersWithLargeInvoices;
import classes.AllCustomersWithLargeInvoices2;
import classes.AllItems;
import classes.ChangeEventType;
import classes.IconType;
import classes.ImageFrom;
import classes.LoginResult;
import classes.MutateResultStatus;
import classes.ReportOutAttribute;
import classes.ReportOutCell;
import classes.ReportOutColumn;
import classes.ReportOutOption;
import classes.ReportOutRow;
import classes.ReportOutput;
import classes.TrackSizeType;
import d3e.core.DFile;
import d3e.core.SchemaConstants;
import gqltosql.schema.DModel;
import gqltosql.schema.DModelType;
import gqltosql.schema.FieldPrimitiveType;
import models.AllCustomersWithLargeInvoices2Request;
import models.AllCustomersWithLargeInvoicesRequest;
import models.AllItemsRequest;
import models.AnonymousUser;
import models.Avatar;
import models.Customer;
import models.D3EImage;
import models.D3EMessage;
import models.EmailMessage;
import models.Invoice;
import models.InvoiceItem;
import models.OneTimePassword;
import models.PushNotification;
import models.ReportConfig;
import models.ReportConfigOption;
import models.SMSMessage;
import models.User;
import models.UserSession;

@org.springframework.stereotype.Service
public class ModelSchema extends AbstractModelSchema {
  protected void createAllEnums() {
    addEnum(MutateResultStatus.class, SchemaConstants.MutateResultStatus);
    addEnum(ChangeEventType.class, SchemaConstants.ChangeEventType);
    addEnum(TrackSizeType.class, SchemaConstants.TrackSizeType);
    addEnum(IconType.class, SchemaConstants.IconType);
    addEnum(ImageFrom.class, SchemaConstants.ImageFrom);
  }

  protected void createAllTables() {
    addTable(
        new DModel<DFile>(
            "DFile", SchemaConstants.DFile, 3, 0, "_dfile", DModelType.MODEL, () -> new DFile()));
    addTable(
        new DModel<AllCustomersWithLargeInvoices2Request>(
                "AllCustomersWithLargeInvoices2Request",
                SchemaConstants.AllCustomersWithLargeInvoices2Request,
                1,
                0,
                "_all_customers_with_large_invoices2request",
                DModelType.MODEL,
                () -> new AllCustomersWithLargeInvoices2Request())
            .trans());
    addTable(
        new DModel<AllCustomersWithLargeInvoicesRequest>(
                "AllCustomersWithLargeInvoicesRequest",
                SchemaConstants.AllCustomersWithLargeInvoicesRequest,
                1,
                0,
                "_all_customers_with_large_invoices_request",
                DModelType.MODEL,
                () -> new AllCustomersWithLargeInvoicesRequest())
            .trans());
    addTable(
        new DModel<AllItemsRequest>(
                "AllItemsRequest",
                SchemaConstants.AllItemsRequest,
                1,
                0,
                "_all_items_request",
                DModelType.MODEL,
                () -> new AllItemsRequest())
            .trans());
    addTable(
        new DModel<AnonymousUser>(
            "AnonymousUser",
            SchemaConstants.AnonymousUser,
            0,
            2,
            "_anonymous_user",
            DModelType.MODEL,
            () -> new AnonymousUser()));
    addTable(
        new DModel<Avatar>(
            "Avatar",
            SchemaConstants.Avatar,
            2,
            0,
            "_avatar",
            DModelType.MODEL,
            () -> new Avatar()));
    addTable(
        new DModel<Customer>(
            "Customer",
            SchemaConstants.Customer,
            5,
            0,
            "_customer",
            DModelType.MODEL,
            () -> new Customer()));
    addTable(
        new DModel<D3EImage>(
                "D3EImage",
                SchemaConstants.D3EImage,
                4,
                0,
                "_d3eimage",
                DModelType.MODEL,
                () -> new D3EImage())
            .emb());
    addTable(
        new DModel<D3EMessage>(
                "D3EMessage", SchemaConstants.D3EMessage, 4, 0, "_d3emessage", DModelType.MODEL)
            .trans());
    addTable(
        new DModel<EmailMessage>(
                "EmailMessage",
                SchemaConstants.EmailMessage,
                6,
                4,
                "_email_message",
                DModelType.MODEL,
                () -> new EmailMessage())
            .trans());
    addTable(
        new DModel<Invoice>(
            "Invoice",
            SchemaConstants.Invoice,
            4,
            0,
            "_invoice",
            DModelType.MODEL,
            () -> new Invoice()));
    addTable(
        new DModel<InvoiceItem>(
            "InvoiceItem",
            SchemaConstants.InvoiceItem,
            3,
            0,
            "_invoice_item",
            DModelType.MODEL,
            () -> new InvoiceItem()));
    addTable(
        new DModel<OneTimePassword>(
            "OneTimePassword",
            SchemaConstants.OneTimePassword,
            9,
            0,
            "_one_time_password",
            DModelType.MODEL,
            () -> new OneTimePassword()));
    addTable(
        new DModel<PushNotification>(
                "PushNotification",
                SchemaConstants.PushNotification,
                4,
                0,
                "_push_notification",
                DModelType.MODEL,
                () -> new PushNotification())
            .trans());
    addTable(
        new DModel<ReportConfig>(
            "ReportConfig",
            SchemaConstants.ReportConfig,
            2,
            0,
            "_report_config",
            DModelType.MODEL,
            () -> new ReportConfig()));
    addTable(
        new DModel<ReportConfigOption>(
            "ReportConfigOption",
            SchemaConstants.ReportConfigOption,
            2,
            0,
            "_report_config_option",
            DModelType.MODEL,
            () -> new ReportConfigOption()));
    addTable(
        new DModel<SMSMessage>(
                "SMSMessage",
                SchemaConstants.SMSMessage,
                1,
                4,
                "_smsmessage",
                DModelType.MODEL,
                () -> new SMSMessage())
            .trans());
    addTable(new DModel<User>("User", SchemaConstants.User, 2, 0, "_user", DModelType.MODEL));
    addTable(
        new DModel<UserSession>(
            "UserSession", SchemaConstants.UserSession, 1, 0, "_user_session", DModelType.MODEL));
    addTable(
        new DModel<ReportOutput>(
            "ReportOutput",
            SchemaConstants.ReportOutput,
            5,
            0,
            null,
            DModelType.STRUCT,
            () -> new ReportOutput()));
    addTable(
        new DModel<ReportOutOption>(
            "ReportOutOption",
            SchemaConstants.ReportOutOption,
            2,
            0,
            null,
            DModelType.STRUCT,
            () -> new ReportOutOption()));
    addTable(
        new DModel<ReportOutColumn>(
            "ReportOutColumn",
            SchemaConstants.ReportOutColumn,
            3,
            0,
            null,
            DModelType.STRUCT,
            () -> new ReportOutColumn()));
    addTable(
        new DModel<ReportOutAttribute>(
            "ReportOutAttribute",
            SchemaConstants.ReportOutAttribute,
            2,
            0,
            null,
            DModelType.STRUCT,
            () -> new ReportOutAttribute()));
    addTable(
        new DModel<ReportOutRow>(
            "ReportOutRow",
            SchemaConstants.ReportOutRow,
            4,
            0,
            null,
            DModelType.STRUCT,
            () -> new ReportOutRow()));
    addTable(
        new DModel<ReportOutCell>(
            "ReportOutCell",
            SchemaConstants.ReportOutCell,
            4,
            0,
            null,
            DModelType.STRUCT,
            () -> new ReportOutCell()));
    addTable(
        new DModel<LoginResult>(
            "LoginResult",
            SchemaConstants.LoginResult,
            4,
            0,
            null,
            DModelType.STRUCT,
            () -> new LoginResult()));
    addTable(
        new DModel<AllCustomersWithAgedGuardians>(
            "AllCustomersWithAgedGuardians",
            SchemaConstants.AllCustomersWithAgedGuardians,
            1,
            0,
            null,
            DModelType.STRUCT,
            () -> new AllCustomersWithAgedGuardians()));
    addTable(
        new DModel<AllCustomersWithAgedGuardians2>(
            "AllCustomersWithAgedGuardians2",
            SchemaConstants.AllCustomersWithAgedGuardians2,
            1,
            0,
            null,
            DModelType.STRUCT,
            () -> new AllCustomersWithAgedGuardians2()));
    addTable(
        new DModel<AllCustomersWithLargeInvoices>(
            "AllCustomersWithLargeInvoices",
            SchemaConstants.AllCustomersWithLargeInvoices,
            1,
            0,
            null,
            DModelType.STRUCT,
            () -> new AllCustomersWithLargeInvoices()));
    addTable(
        new DModel<AllCustomersWithLargeInvoices2>(
            "AllCustomersWithLargeInvoices2",
            SchemaConstants.AllCustomersWithLargeInvoices2,
            1,
            0,
            null,
            DModelType.STRUCT,
            () -> new AllCustomersWithLargeInvoices2()));
    addTable(
        new DModel<AllItems>(
            "AllItems",
            SchemaConstants.AllItems,
            1,
            0,
            null,
            DModelType.STRUCT,
            () -> new AllItems()));
    addDFileFields();
  }

  protected void addFields() {
    new ModelSchema1(allTypes).createAllTables();
    new StructSchema1(allTypes).createAllTables();
  }

  protected void recordAllChannels() {
    recordNumChannels(0);
  }
}
