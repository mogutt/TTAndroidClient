
package com.mogujie.tt.exception;

@SuppressWarnings("serial")
public class BaseException extends RuntimeException {

    public BaseException() {
        super();
    }

    public BaseException(String msg) {
        super(msg);
    }

    public BaseException(Throwable ex) {
        super(ex);
    }

    public BaseException(String msg, Throwable ex) {
        super(msg, ex);
    }
}
