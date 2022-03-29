package tudbut.mod.client.ttcp.utils.isbpl;

class ISBPLStop
extends RuntimeException {
    int amount;

    public ISBPLStop(int amount) {
        this.amount = amount - 1;
    }
}
