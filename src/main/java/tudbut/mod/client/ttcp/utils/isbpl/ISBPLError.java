package tudbut.mod.client.ttcp.utils.isbpl;

class ISBPLError
extends RuntimeException {
    final String type;
    final String message;

    public ISBPLError(String type, String message) {
        this.type = type;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return this.type + ": " + this.message;
    }
}
