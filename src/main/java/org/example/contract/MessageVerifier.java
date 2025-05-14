package org.example.contract;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple3;
import org.web3j.tuples.generated.Tuple4;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/hyperledger-web3j/web3j/tree/main/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 1.6.3.
 */
@SuppressWarnings("rawtypes")
public class MessageVerifier extends Contract {
    public static final String BINARY = "60806040526000600155348015601457600080fd5b506112f1806100246000396000f3fe608060405234801561001057600080fd5b50600436106100885760003560e01c806355429dad1161005b57806355429dad14610140578063ba86a10d1461015c578063dfceceae1461018c578063eb74d835146101bc57610088565b80630d80fefd1461008d5780633212082a146100bf5780633dbcc8d1146100f2578063469c811014610110575b600080fd5b6100a760048036038101906100a291906109ba565b6101ec565b6040516100b693929190610ac7565b60405180910390f35b6100d960048036038101906100d491906109ba565b6102be565b6040516100e99493929190610b05565b60405180910390f35b6100fa6103f1565b6040516101079190610b51565b60405180910390f35b61012a60048036038101906101259190610ca1565b6103f7565b6040516101379190610b51565b60405180910390f35b61015a600480360381019061015591906109ba565b6104e4565b005b610176600480360381019061017191906109ba565b6106f0565b6040516101839190610cea565b60405180910390f35b6101a660048036038101906101a191906109ba565b6107db565b6040516101b39190610dca565b60405180910390f35b6101d660048036038101906101d19190610e18565b6108c2565b6040516101e39190610e73565b60405180910390f35b60006020528060005260406000206000915090508060000160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff169080600101805461023590610ebd565b80601f016020809104026020016040519081016040528092919081815260200182805461026190610ebd565b80156102ae5780601f10610283576101008083540402835291602001916102ae565b820191906000526020600020905b81548152906001019060200180831161029157829003601f168201915b5050505050908060020154905083565b600060606000806001548510610309576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040161030090610f3a565b60405180910390fd5b600080600087815260200190815260200160002090508060000160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16816001018260020154836003018054905082805461036190610ebd565b80601f016020809104026020016040519081016040528092919081815260200182805461038d90610ebd565b80156103da5780601f106103af576101008083540402835291602001916103da565b820191906000526020600020905b8154815290600101906020018083116103bd57829003601f168201915b505050505092509450945094509450509193509193565b60015481565b60008060015490506001600081548092919061041290610f89565b919050555060008060008381526020019081526020016000209050338160000160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555083816001019081610481919061117d565b504281600201819055503373ffffffffffffffffffffffffffffffffffffffff16827fa758af4732a8088ab75915c760fabb11b22db520c32f4ee5cdc6651c268d3afa866040516104d29190610cea565b60405180910390a38192505050919050565b6001548110610528576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040161051f90610f3a565b60405180910390fd5b60008082815260200190815260200160002060040160003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060009054906101000a900460ff16156105c8576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016105bf9061129b565b60405180910390fd5b600080828152602001908152602001600020600301339080600181540180825580915050600190039060005260206000200160009091909190916101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550600160008083815260200190815260200160002060040160003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060006101000a81548160ff0219169083151502179055503373ffffffffffffffffffffffffffffffffffffffff16817f6a9fddc931c8be10d88d763dc9d43768ee1f51819274d495ae7da64c6108b03460405160405180910390a350565b60606001548210610736576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040161072d90610f3a565b60405180910390fd5b600080838152602001908152602001600020600101805461075690610ebd565b80601f016020809104026020016040519081016040528092919081815260200182805461078290610ebd565b80156107cf5780601f106107a4576101008083540402835291602001916107cf565b820191906000526020600020905b8154815290600101906020018083116107b257829003601f168201915b50505050509050919050565b60606001548210610821576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040161081890610f3a565b60405180910390fd5b6000808381526020019081526020016000206003018054806020026020016040519081016040528092919081815260200182805480156108b657602002820191906000526020600020905b8160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001906001019080831161086c575b50505050509050919050565b60006001548310610908576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016108ff90610f3a565b60405180910390fd5b60008084815260200190815260200160002060040160008373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060009054906101000a900460ff16905092915050565b6000604051905090565b600080fd5b600080fd5b6000819050919050565b61099781610984565b81146109a257600080fd5b50565b6000813590506109b48161098e565b92915050565b6000602082840312156109d0576109cf61097a565b5b60006109de848285016109a5565b91505092915050565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b6000610a12826109e7565b9050919050565b610a2281610a07565b82525050565b600081519050919050565b600082825260208201905092915050565b60005b83811015610a62578082015181840152602081019050610a47565b60008484015250505050565b6000601f19601f8301169050919050565b6000610a8a82610a28565b610a948185610a33565b9350610aa4818560208601610a44565b610aad81610a6e565b840191505092915050565b610ac181610984565b82525050565b6000606082019050610adc6000830186610a19565b8181036020830152610aee8185610a7f565b9050610afd6040830184610ab8565b949350505050565b6000608082019050610b1a6000830187610a19565b8181036020830152610b2c8186610a7f565b9050610b3b6040830185610ab8565b610b486060830184610ab8565b95945050505050565b6000602082019050610b666000830184610ab8565b92915050565b600080fd5b600080fd5b7f4e487b7100000000000000000000000000000000000000000000000000000000600052604160045260246000fd5b610bae82610a6e565b810181811067ffffffffffffffff82111715610bcd57610bcc610b76565b5b80604052505050565b6000610be0610970565b9050610bec8282610ba5565b919050565b600067ffffffffffffffff821115610c0c57610c0b610b76565b5b610c1582610a6e565b9050602081019050919050565b82818337600083830152505050565b6000610c44610c3f84610bf1565b610bd6565b905082815260208101848484011115610c6057610c5f610b71565b5b610c6b848285610c22565b509392505050565b600082601f830112610c8857610c87610b6c565b5b8135610c98848260208601610c31565b91505092915050565b600060208284031215610cb757610cb661097a565b5b600082013567ffffffffffffffff811115610cd557610cd461097f565b5b610ce184828501610c73565b91505092915050565b60006020820190508181036000830152610d048184610a7f565b905092915050565b600081519050919050565b600082825260208201905092915050565b6000819050602082019050919050565b610d4181610a07565b82525050565b6000610d538383610d38565b60208301905092915050565b6000602082019050919050565b6000610d7782610d0c565b610d818185610d17565b9350610d8c83610d28565b8060005b83811015610dbd578151610da48882610d47565b9750610daf83610d5f565b925050600181019050610d90565b5085935050505092915050565b60006020820190508181036000830152610de48184610d6c565b905092915050565b610df581610a07565b8114610e0057600080fd5b50565b600081359050610e1281610dec565b92915050565b60008060408385031215610e2f57610e2e61097a565b5b6000610e3d858286016109a5565b9250506020610e4e85828601610e03565b9150509250929050565b60008115159050919050565b610e6d81610e58565b82525050565b6000602082019050610e886000830184610e64565b92915050565b7f4e487b7100000000000000000000000000000000000000000000000000000000600052602260045260246000fd5b60006002820490506001821680610ed557607f821691505b602082108103610ee857610ee7610e8e565b5b50919050565b7f4d65737361676520646f6573206e6f742065786973742e000000000000000000600082015250565b6000610f24601783610a33565b9150610f2f82610eee565b602082019050919050565b60006020820190508181036000830152610f5381610f17565b9050919050565b7f4e487b7100000000000000000000000000000000000000000000000000000000600052601160045260246000fd5b6000610f9482610984565b91507fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff8203610fc657610fc5610f5a565b5b600182019050919050565b60008190508160005260206000209050919050565b60006020601f8301049050919050565b600082821b905092915050565b6000600883026110337fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff82610ff6565b61103d8683610ff6565b95508019841693508086168417925050509392505050565b6000819050919050565b600061107a61107561107084610984565b611055565b610984565b9050919050565b6000819050919050565b6110948361105f565b6110a86110a082611081565b848454611003565b825550505050565b600090565b6110bd6110b0565b6110c881848461108b565b505050565b5b818110156110ec576110e16000826110b5565b6001810190506110ce565b5050565b601f8211156111315761110281610fd1565b61110b84610fe6565b8101602085101561111a578190505b61112e61112685610fe6565b8301826110cd565b50505b505050565b600082821c905092915050565b600061115460001984600802611136565b1980831691505092915050565b600061116d8383611143565b9150826002028217905092915050565b61118682610a28565b67ffffffffffffffff81111561119f5761119e610b76565b5b6111a98254610ebd565b6111b48282856110f0565b600060209050601f8311600181146111e757600084156111d5578287015190505b6111df8582611161565b865550611247565b601f1984166111f586610fd1565b60005b8281101561121d578489015182556001820191506020850194506020810190506111f8565b8683101561123a5784890151611236601f891682611143565b8355505b6001600288020188555050505b505050505050565b7f416c7265616479207369676e65642e0000000000000000000000000000000000600082015250565b6000611285600f83610a33565b91506112908261124f565b602082019050919050565b600060208201905081810360008301526112b481611278565b905091905056fea2646970667358221220b96d5ef8e42101932bd319edba06bdacb416597b816e1e97cdba17598bd7de2e64736f6c634300081a0033";

    private static String librariesLinkedBinary;

    public static final String FUNC_GETMESSAGEDETAILS = "getMessageDetails";

    public static final String FUNC_GETMESSAGEHASH = "getMessageHash";

    public static final String FUNC_GETSIGNERS = "getSigners";

    public static final String FUNC_HASUSERSIGNED = "hasUserSigned";

    public static final String FUNC_MESSAGECOUNT = "messageCount";

    public static final String FUNC_MESSAGES = "messages";

    public static final String FUNC_SENDMESSAGE = "sendMessage";

    public static final String FUNC_SIGNMESSAGE = "signMessage";

    public static final Event MESSAGESENT_EVENT = new Event("MessageSent", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Utf8String>() {}));
    ;

    public static final Event MESSAGESIGNED_EVENT = new Event("MessageSigned", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Address>(true) {}));
    ;

    @Deprecated
    protected MessageVerifier(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected MessageVerifier(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected MessageVerifier(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected MessageVerifier(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static List<MessageSentEventResponse> getMessageSentEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(MESSAGESENT_EVENT, transactionReceipt);
        ArrayList<MessageSentEventResponse> responses = new ArrayList<MessageSentEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            MessageSentEventResponse typedResponse = new MessageSentEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.messageId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.sender = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.messageHash = (String) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static MessageSentEventResponse getMessageSentEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(MESSAGESENT_EVENT, log);
        MessageSentEventResponse typedResponse = new MessageSentEventResponse();
        typedResponse.log = log;
        typedResponse.messageId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.sender = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.messageHash = (String) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<MessageSentEventResponse> messageSentEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getMessageSentEventFromLog(log));
    }

    public Flowable<MessageSentEventResponse> messageSentEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(MESSAGESENT_EVENT));
        return messageSentEventFlowable(filter);
    }

    public static List<MessageSignedEventResponse> getMessageSignedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(MESSAGESIGNED_EVENT, transactionReceipt);
        ArrayList<MessageSignedEventResponse> responses = new ArrayList<MessageSignedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            MessageSignedEventResponse typedResponse = new MessageSignedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.messageId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.signer = (String) eventValues.getIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static MessageSignedEventResponse getMessageSignedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(MESSAGESIGNED_EVENT, log);
        MessageSignedEventResponse typedResponse = new MessageSignedEventResponse();
        typedResponse.log = log;
        typedResponse.messageId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.signer = (String) eventValues.getIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<MessageSignedEventResponse> messageSignedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getMessageSignedEventFromLog(log));
    }

    public Flowable<MessageSignedEventResponse> messageSignedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(MESSAGESIGNED_EVENT));
        return messageSignedEventFlowable(filter);
    }

    public RemoteFunctionCall<Tuple4<String, String, BigInteger, BigInteger>> getMessageDetails(
            BigInteger messageId) {
        final Function function = new Function(FUNC_GETMESSAGEDETAILS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(messageId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
        return new RemoteFunctionCall<Tuple4<String, String, BigInteger, BigInteger>>(function,
                new Callable<Tuple4<String, String, BigInteger, BigInteger>>() {
                    @Override
                    public Tuple4<String, String, BigInteger, BigInteger> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple4<String, String, BigInteger, BigInteger>(
                                (String) results.get(0).getValue(), 
                                (String) results.get(1).getValue(), 
                                (BigInteger) results.get(2).getValue(), 
                                (BigInteger) results.get(3).getValue());
                    }
                });
    }

    public RemoteFunctionCall<String> getMessageHash(BigInteger messageId) {
        final Function function = new Function(FUNC_GETMESSAGEHASH, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(messageId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<List> getSigners(BigInteger messageId) {
        final Function function = new Function(FUNC_GETSIGNERS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(messageId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Address>>() {}));
        return new RemoteFunctionCall<List>(function,
                new Callable<List>() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public List call() throws Exception {
                        List<Type> result = (List<Type>) executeCallSingleValueReturn(function, List.class);
                        return convertToNative(result);
                    }
                });
    }

    public RemoteFunctionCall<Boolean> hasUserSigned(BigInteger messageId, String user) {
        final Function function = new Function(FUNC_HASUSERSIGNED, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(messageId), 
                new org.web3j.abi.datatypes.Address(160, user)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<BigInteger> messageCount() {
        final Function function = new Function(FUNC_MESSAGECOUNT, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<Tuple3<String, String, BigInteger>> messages(BigInteger param0) {
        final Function function = new Function(FUNC_MESSAGES, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}));
        return new RemoteFunctionCall<Tuple3<String, String, BigInteger>>(function,
                new Callable<Tuple3<String, String, BigInteger>>() {
                    @Override
                    public Tuple3<String, String, BigInteger> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple3<String, String, BigInteger>(
                                (String) results.get(0).getValue(), 
                                (String) results.get(1).getValue(), 
                                (BigInteger) results.get(2).getValue());
                    }
                });
    }

    public RemoteFunctionCall<TransactionReceipt> sendMessage(String messageHash) {
        final Function function = new Function(
                FUNC_SENDMESSAGE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(messageHash)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> signMessage(BigInteger messageId) {
        final Function function = new Function(
                FUNC_SIGNMESSAGE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(messageId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static MessageVerifier load(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        return new MessageVerifier(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static MessageVerifier load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new MessageVerifier(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static MessageVerifier load(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        return new MessageVerifier(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static MessageVerifier load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new MessageVerifier(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<MessageVerifier> deploy(Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        return deployRemoteCall(MessageVerifier.class, web3j, credentials, contractGasProvider, getDeploymentBinary(), "");
    }

    @Deprecated
    public static RemoteCall<MessageVerifier> deploy(Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(MessageVerifier.class, web3j, credentials, gasPrice, gasLimit, getDeploymentBinary(), "");
    }

    public static RemoteCall<MessageVerifier> deploy(Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(MessageVerifier.class, web3j, transactionManager, contractGasProvider, getDeploymentBinary(), "");
    }

    @Deprecated
    public static RemoteCall<MessageVerifier> deploy(Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(MessageVerifier.class, web3j, transactionManager, gasPrice, gasLimit, getDeploymentBinary(), "");
    }

    private static String getDeploymentBinary() {
        if (librariesLinkedBinary != null) {
            return librariesLinkedBinary;
        } else {
            return BINARY;
        }
    }

    public static class MessageSentEventResponse extends BaseEventResponse {
        public BigInteger messageId;

        public String sender;

        public String messageHash;
    }

    public static class MessageSignedEventResponse extends BaseEventResponse {
        public BigInteger messageId;

        public String signer;
    }
}
