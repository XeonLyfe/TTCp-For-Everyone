package tudbut.mod.client.ttcp.utils.isbpl;

class ISBPLType {
    static int gid = -2;
    int id = gid++;
    String name;

    public ISBPLType(String name) {
        this.name = name;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ISBPLType)) {
            return false;
        }
        ISBPLType type = (ISBPLType)o;
        return this.id == type.id;
    }

    public int hashCode() {
        return this.id;
    }

    public String toString() {
        return "ISBPLType{id=" + this.id + ", name='" + this.name + '\'' + '}';
    }
}
