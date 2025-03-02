package openerp.openerpresourceserver.wms.exception;

public class DataExistedException extends RuntimeException {
    public DataExistedException(String message) {
        super(message);
    }
}
