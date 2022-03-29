package tudbut.mod.client.ttcp.utils.isbpl;

import java.util.Arrays;
import tudbut.mod.client.ttcp.utils.isbpl.ISBPLError;
import tudbut.mod.client.ttcp.utils.isbpl.ISBPLType;

class ISBPLObject {
    final ISBPLType type;
    final Object object;

    public ISBPLObject(ISBPLType type, Object object) {
        this.type = type;
        this.object = object;
    }

    public boolean isTruthy() {
        return this.object != null && this.object != Integer.valueOf(0);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ISBPLObject)) {
            return false;
        }
        ISBPLObject object = (ISBPLObject)o;
        if (this.object == object.object) {
            return true;
        }
        if (this.object == null) {
            return false;
        }
        if (object.object == null) {
            return false;
        }
        if (this.object.getClass().isArray() || object.object.getClass().isArray()) {
            if (this.object.getClass().isArray() && object.object.getClass().isArray()) {
                return Arrays.equals((Object[])this.object, (Object[])object.object);
            }
            return false;
        }
        return this.object.equals(object.object);
    }

    public void checkType(ISBPLType wanted) {
        if (wanted.id != this.type.id) {
            throw new ISBPLError("IncompatibleTypes", "Incompatible types: " + this.type.name + " - " + wanted.name);
        }
    }

    public int checkTypeMulti(ISBPLType ... wanted) {
        int f = -1;
        String wantedNames = "";
        for (int i = 0; i < wanted.length; ++i) {
            wantedNames = wantedNames + " " + wanted[i].name;
            if (wanted[i].id != this.type.id) continue;
            f = i;
            break;
        }
        if (f == -1) {
            throw new ISBPLError("IncompatibleTypes", "Incompatible types: " + this.type.name + " - " + wantedNames.substring(1));
        }
        return f;
    }

    public String toString() {
        if (this.type != null && this.object instanceof ISBPLObject[]) {
            try {
                return "ISBPLObject{type=" + this.type + ", object=" + Arrays.toString((ISBPLObject[])this.object) + '}';
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "ISBPLObject{type=" + this.type + ", object=" + this.object + '}';
    }

    public double toDouble() {
        if (this.object instanceof Integer) {
            return ((Integer)this.object).intValue();
        }
        if (this.object instanceof Long) {
            return ((Long)this.object).longValue();
        }
        if (this.object instanceof Character) {
            return ((Character)this.object).charValue();
        }
        if (this.object instanceof Byte) {
            return Byte.toUnsignedInt((Byte)this.object);
        }
        if (this.object instanceof Float) {
            return ((Float)this.object).floatValue();
        }
        if (this.object instanceof Double) {
            return (Double)this.object;
        }
        throw new ISBPLError("InvalidArgument", "The argument is not a number.");
    }

    public long toLong() {
        if (this.object instanceof Integer) {
            return ((Integer)this.object).intValue();
        }
        if (this.object instanceof Long) {
            return (Long)this.object;
        }
        if (this.object instanceof Character) {
            return ((Character)this.object).charValue();
        }
        if (this.object instanceof Byte) {
            return Byte.toUnsignedInt((Byte)this.object);
        }
        if (this.object instanceof Float) {
            return (long)((Float)this.object).floatValue();
        }
        if (this.object instanceof Double) {
            return (long)((Double)this.object).doubleValue();
        }
        throw new ISBPLError("InvalidArgument", "The argument is not a number.");
    }

    public Object negative() {
        if (this.object instanceof Integer) {
            return -((Integer)this.object).intValue();
        }
        if (this.object instanceof Long) {
            return -((Long)this.object).longValue();
        }
        if (this.object instanceof Float) {
            return Float.valueOf(-((Float)this.object).floatValue());
        }
        if (this.object instanceof Double) {
            return -((Double)this.object).doubleValue();
        }
        throw new ISBPLError("InvalidArgument", "This type of number can't be negated!");
    }
}
