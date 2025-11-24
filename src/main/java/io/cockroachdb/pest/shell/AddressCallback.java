package io.cockroachdb.pest.shell;

public interface AddressCallback {
    String firstNodeIP();

    String serviceAddr(int id);

    String advertiseAddr(int id);

    String advertiseProxyAddr(int id);

    String listenAddr(int id);

    String sqlAddr(int id);

    String httpAddr(int id);
}
