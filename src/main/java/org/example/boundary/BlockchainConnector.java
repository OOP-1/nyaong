package org.example.boundary;

import org.example.config.EnvLoader;
import org.example.contract.MessageVerifier;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;

public class BlockchainConnector {
    private static final String RPC_URL = EnvLoader.get("GETH_URL");
    private static final String PRIVATE_KEY = EnvLoader.get("GETH_KEY");
    private static final String CONTRACT_ADDRESS = EnvLoader.get("GETH_ADDRESS");
    private static final long CHAIN_ID = Long.parseLong(EnvLoader.get("GETH_CHAIN_ID"));

    private final Web3j web3j;
    private final Credentials credentials;
    private final MessageVerifier contract;

    public BlockchainConnector() {
        this.web3j = Web3j.build(new HttpService(RPC_URL));
        this.credentials = Credentials.create(PRIVATE_KEY);
        RawTransactionManager txManager = new RawTransactionManager(web3j, credentials, CHAIN_ID);

        this.contract = MessageVerifier.load(
                CONTRACT_ADDRESS,
                web3j,
                txManager,
                new DefaultGasProvider()
        );

        System.out.println("Contract deployed at: " + contract.getContractAddress());
    }

    public MessageVerifier getContract() {
        return contract;
    }

    public Web3j getWeb3j() {
        return web3j;
    }

    public Credentials getCredentials() {
        return credentials;
    }
}