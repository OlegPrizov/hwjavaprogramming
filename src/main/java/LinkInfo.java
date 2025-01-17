class LinkInfo {
    private String longLink;
    private long linkCreatedDate;
    private int limit;

    public LinkInfo(String longLink, long linkCreatedDate, int limit) {
        this.longLink = longLink;
        this.linkCreatedDate = linkCreatedDate;
        this.limit = limit;
    }

    public String getLongLink() {
        return longLink;
    }

    public long getLinkCreatedDate() {
        return linkCreatedDate;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}