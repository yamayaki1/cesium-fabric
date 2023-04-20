package de.yamayaki.cesium.common.db.lightning;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.lmdb.LMDB;
import org.lwjgl.util.lmdb.MDBVal;

import java.nio.ByteBuffer;
import java.util.Iterator;

public class Csr implements Iterator<ByteBuffer>, AutoCloseable {
    private final long id;

    Csr(long pointer) {
        this.id = pointer;
    }

    @Override
    public boolean hasNext() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            MDBVal key = new MDBVal(stack.malloc(MDBVal.SIZEOF));
            MDBVal value = new MDBVal(stack.malloc(MDBVal.SIZEOF));

            int result = LMDB.mdb_cursor_get(this.id, key, value, LMDB.MDB_NEXT);

            if (result == LMDB.MDB_NOTFOUND) {
                return false;
            }

            LmdbUtil.checkError(result);

            return true;
        } catch (LmdbException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public ByteBuffer next() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            MDBVal key = new MDBVal(stack.malloc(MDBVal.SIZEOF));
            MDBVal value = new MDBVal(stack.malloc(MDBVal.SIZEOF));

            LmdbUtil.checkError(LMDB.mdb_cursor_get(this.id, key, value, LMDB.MDB_GET_CURRENT));
            return key.mv_data();
        } catch (LmdbException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public void close() {
        LMDB.mdb_cursor_close(this.id);
    }
}
