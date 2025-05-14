package org.example.config;

import org.example.contract.MessageVerifier;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;

public class ContractPublisher {
    public static void main(String[] args) throws Exception {
        Web3j web3j = Web3j.build(new HttpService(EnvLoader.get("GETH_URL")));
        Credentials credentials = Credentials.create(EnvLoader.get("GETH_KEY"));
        Long CHAIN_ID = Long.parseLong(EnvLoader.get("GETH_CHAIN_ID"));

        RawTransactionManager txManager = new RawTransactionManager(web3j, credentials, CHAIN_ID);

        MessageVerifier contract = MessageVerifier.deploy(
                web3j,
                txManager,
                new DefaultGasProvider()
        ).send();

        String contractAddress = contract.getContractAddress();
        System.out.println("컨트랙트 주소: " + contractAddress);
    }
}
