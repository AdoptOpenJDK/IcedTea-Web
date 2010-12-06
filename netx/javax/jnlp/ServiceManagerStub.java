package javax.jnlp;

public interface ServiceManagerStub {

    public java.lang.Object lookup(java.lang.String name) throws UnavailableServiceException;

    public java.lang.String[] getServiceNames();

}
