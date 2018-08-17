package com.perxcel.confluence.tools;

import java.util.Map;

public class PageResult {

    private String id;

    private String type;

    private String status;

    private String title;

    private ChildTypes childTypes;

    private PageBody body;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ChildTypes getChildTypes() {
        return childTypes;
    }

    public void setChildTypes(ChildTypes childTypes) {
        this.childTypes = childTypes;
    }

    public PageBody getBody() {
        return body;
    }

    public void setBody(PageBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "PageResult{"
               + "id='"
               + id
               + '\''
               + ", type='"
               + type
               + '\''
               + ", status='"
               + status
               + '\''
               + ", title='"
               + title
               + '\''
               + ", childTypes="
               + childTypes
               + ", body="
               + body
               + '}';
    }

    public static class ChildTypes {
        private Map attachment;

        private Map page;

        public Map getAttachment() {
            return attachment;
        }

        public void setAttachment(Map attachment) {
            this.attachment = attachment;
        }

        public Map getPage() {
            return page;
        }

        public void setPage(Map page) {
            this.page = page;
        }

        @Override
        public String toString() {
            return "ChildTypes{" + "attachment=" + attachment + ", page=" + page + '}';
        }
    }
}
