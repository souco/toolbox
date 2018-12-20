package cc.souco.toolbox.common;

import java.util.HashMap;
import java.util.Map;

public class Ret extends HashMap {

    private static final String SUCCESS = "success";
    private static final String MESSAGE = "msg";

    {
        set("success", true);
        set("msg", "操作成功");
    }

    public Ret() {}

    public static Ret by(Object key, Object value) {
        return new Ret().set(key, value);
    }

    public static Ret ok() {
        return new Ret();
    }

    public Ret setOk(){
        return this.set(SUCCESS, true);
    }

    public static Ret fail(String msg) {
        return new Ret().setFail(msg);
    }

    public Ret setFail(String msg) {
        this.set(SUCCESS, false);
        this.set(MESSAGE, msg);
        return this;
    }

    public Ret set(Object key, Object value) {
        super.put(key, value);
        return this;
    }

    public Ret set(Map map) {
        super.putAll(map);
        return this;
    }

    public Ret set(Ret ret) {
        super.putAll(ret);
        return this;
    }
}
