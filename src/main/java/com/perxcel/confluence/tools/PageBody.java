package com.perxcel.confluence.tools;

public class PageBody {
    private Storage storage;

    public Storage getStorage() {
        return storage;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    @Override
    public String toString() {
        return "PageBody{" + "storage=" + storage + '}';
    }

    public static class Storage {
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "Storage{" + "value='" + value + '\'' + '}';
        }
    }
}
