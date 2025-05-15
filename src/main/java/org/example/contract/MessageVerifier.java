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
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
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
    public static final String BINARY = "60806040526000600155348015601457600080fd5b5061127d806100246000396000f3fe608060405234801561001057600080fd5b50600436106100885760003560e01c80633dbcc8d11161005b5780633dbcc8d114610152578063b8114ff214610170578063ba86a10d1461018c578063dfceceae146101bc57610088565b80630d80fefd1461008d5780633212082a146100bf57806332d46ae7146100f25780633411f70214610122575b600080fd5b6100a760048036038101906100a29190610853565b6101ec565b6040516100b693929190610938565b60405180910390f35b6100d960048036038101906100d49190610853565b61029e565b6040516100e99493929190610976565b60405180910390f35b61010c60048036038101906101079190610af7565b6103b1565b6040516101199190610b6e565b60405180910390f35b61013c60048036038101906101379190610bb5565b61043e565b6040516101499190610c11565b60405180910390f35b61015a6104dd565b6040516101679190610c11565b60405180910390f35b61018a60048036038101906101859190610af7565b6104e3565b005b6101a660048036038101906101a19190610853565b610674565b6040516101b39190610c2c565b60405180910390f35b6101d660048036038101906101d19190610853565b6106d7565b6040516101e39190610d53565b60405180910390f35b600060205280600052604060002060009150905080600001805461020f90610da4565b80601f016020809104026020016040519081016040528092919081815260200182805461023b90610da4565b80156102885780601f1061025d57610100808354040283529160200191610288565b820191906000526020600020905b81548152906001019060200180831161026b57829003601f168201915b5050505050908060010154908060020154905083565b6060600080600060015485106102e9576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016102e090610e21565b60405180910390fd5b600080600087815260200190815260200160002090508060000181600101548260020154836003018054905083805461032190610da4565b80601f016020809104026020016040519081016040528092919081815260200182805461034d90610da4565b801561039a5780601f1061036f5761010080835404028352916020019161039a565b820191906000526020600020905b81548152906001019060200180831161037d57829003601f168201915b505050505093509450945094509450509193509193565b600060015483106103f7576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016103ee90610e21565b60405180910390fd5b6000808481526020019081526020016000206004018260405161041a9190610e7d565b908152602001604051809103902060009054906101000a900460ff16905092915050565b60008060015490506001600081548092919061045990610ec3565b9190505550600080600083815260200190815260200160002090508481600001908161048591906110b7565b50838160010181905550428160020181905550817fac009cc0c59bff80c8e73ec8f1d4381b070ccbba04c582bc422cc758cf5fe4ba86866040516104ca929190611189565b60405180910390a2819250505092915050565b60015481565b6001548210610527576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040161051e90610e21565b60405180910390fd5b6000808381526020019081526020016000206004018160405161054a9190610e7d565b908152602001604051809103902060009054906101000a900460ff16156105a6576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040161059d90611205565b60405180910390fd5b600080838152602001908152602001600020600301819080600181540180825580915050600190039060005260206000200160009091909190915090816105ed91906110b7565b506001600080848152602001908152602001600020600401826040516106139190610e7d565b908152602001604051809103902060006101000a81548160ff021916908315150217905550817f0d4069bbb8325eeb8aecf1d1d88c1490fe40e6fc2a664f328239c0620d98cf2d826040516106689190611225565b60405180910390a25050565b600060015482106106ba576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016106b190610e21565b60405180910390fd5b600080838152602001908152602001600020600101549050919050565b6060600154821061071d576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040161071490610e21565b60405180910390fd5b600080838152602001908152602001600020600301805480602002602001604051908101604052809291908181526020016000905b828210156107fe57838290600052602060002001805461077190610da4565b80601f016020809104026020016040519081016040528092919081815260200182805461079d90610da4565b80156107ea5780601f106107bf576101008083540402835291602001916107ea565b820191906000526020600020905b8154815290600101906020018083116107cd57829003601f168201915b505050505081526020019060010190610752565b505050509050919050565b6000604051905090565b600080fd5b600080fd5b6000819050919050565b6108308161081d565b811461083b57600080fd5b50565b60008135905061084d81610827565b92915050565b60006020828403121561086957610868610813565b5b60006108778482850161083e565b91505092915050565b600081519050919050565b600082825260208201905092915050565b60005b838110156108ba57808201518184015260208101905061089f565b60008484015250505050565b6000601f19601f8301169050919050565b60006108e282610880565b6108ec818561088b565b93506108fc81856020860161089c565b610905816108c6565b840191505092915050565b6000819050919050565b61092381610910565b82525050565b6109328161081d565b82525050565b6000606082019050818103600083015261095281866108d7565b9050610961602083018561091a565b61096e6040830184610929565b949350505050565b6000608082019050818103600083015261099081876108d7565b905061099f602083018661091a565b6109ac6040830185610929565b6109b96060830184610929565b95945050505050565b600080fd5b600080fd5b7f4e487b7100000000000000000000000000000000000000000000000000000000600052604160045260246000fd5b610a04826108c6565b810181811067ffffffffffffffff82111715610a2357610a226109cc565b5b80604052505050565b6000610a36610809565b9050610a4282826109fb565b919050565b600067ffffffffffffffff821115610a6257610a616109cc565b5b610a6b826108c6565b9050602081019050919050565b82818337600083830152505050565b6000610a9a610a9584610a47565b610a2c565b905082815260208101848484011115610ab657610ab56109c7565b5b610ac1848285610a78565b509392505050565b600082601f830112610ade57610add6109c2565b5b8135610aee848260208601610a87565b91505092915050565b60008060408385031215610b0e57610b0d610813565b5b6000610b1c8582860161083e565b925050602083013567ffffffffffffffff811115610b3d57610b3c610818565b5b610b4985828601610ac9565b9150509250929050565b60008115159050919050565b610b6881610b53565b82525050565b6000602082019050610b836000830184610b5f565b92915050565b610b9281610910565b8114610b9d57600080fd5b50565b600081359050610baf81610b89565b92915050565b60008060408385031215610bcc57610bcb610813565b5b600083013567ffffffffffffffff811115610bea57610be9610818565b5b610bf685828601610ac9565b9250506020610c0785828601610ba0565b9150509250929050565b6000602082019050610c266000830184610929565b92915050565b6000602082019050610c41600083018461091a565b92915050565b600081519050919050565b600082825260208201905092915050565b6000819050602082019050919050565b600082825260208201905092915050565b6000610c8f82610880565b610c998185610c73565b9350610ca981856020860161089c565b610cb2816108c6565b840191505092915050565b6000610cc98383610c84565b905092915050565b6000602082019050919050565b6000610ce982610c47565b610cf38185610c52565b935083602082028501610d0585610c63565b8060005b85811015610d415784840389528151610d228582610cbd565b9450610d2d83610cd1565b925060208a01995050600181019050610d09565b50829750879550505050505092915050565b60006020820190508181036000830152610d6d8184610cde565b905092915050565b7f4e487b7100000000000000000000000000000000000000000000000000000000600052602260045260246000fd5b60006002820490506001821680610dbc57607f821691505b602082108103610dcf57610dce610d75565b5b50919050565b7f4d65737361676520646f6573206e6f742065786973742e000000000000000000600082015250565b6000610e0b60178361088b565b9150610e1682610dd5565b602082019050919050565b60006020820190508181036000830152610e3a81610dfe565b9050919050565b600081905092915050565b6000610e5782610880565b610e618185610e41565b9350610e7181856020860161089c565b80840191505092915050565b6000610e898284610e4c565b915081905092915050565b7f4e487b7100000000000000000000000000000000000000000000000000000000600052601160045260246000fd5b6000610ece8261081d565b91507fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff8203610f0057610eff610e94565b5b600182019050919050565b60008190508160005260206000209050919050565b60006020601f8301049050919050565b600082821b905092915050565b600060088302610f6d7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff82610f30565b610f778683610f30565b95508019841693508086168417925050509392505050565b6000819050919050565b6000610fb4610faf610faa8461081d565b610f8f565b61081d565b9050919050565b6000819050919050565b610fce83610f99565b610fe2610fda82610fbb565b848454610f3d565b825550505050565b600090565b610ff7610fea565b611002818484610fc5565b505050565b5b818110156110265761101b600082610fef565b600181019050611008565b5050565b601f82111561106b5761103c81610f0b565b61104584610f20565b81016020851015611054578190505b61106861106085610f20565b830182611007565b50505b505050565b600082821c905092915050565b600061108e60001984600802611070565b1980831691505092915050565b60006110a7838361107d565b9150826002028217905092915050565b6110c082610880565b67ffffffffffffffff8111156110d9576110d86109cc565b5b6110e38254610da4565b6110ee82828561102a565b600060209050601f831160018114611121576000841561110f578287015190505b611119858261109b565b865550611181565b601f19841661112f86610f0b565b60005b8281101561115757848901518255600182019150602085019450602081019050611132565b868310156111745784890151611170601f89168261107d565b8355505b6001600288020188555050505b505050505050565b600060408201905081810360008301526111a381856108d7565b90506111b2602083018461091a565b9392505050565b7f416c7265616479207369676e6564206279207468697320757365722e00000000600082015250565b60006111ef601c8361088b565b91506111fa826111b9565b602082019050919050565b6000602082019050818103600083015261121e816111e2565b9050919050565b6000602082019050818103600083015261123f81846108d7565b90509291505056fea2646970667358221220dcc253c52e746d34621f75f2aec92342289364045295ec37c9cfc57ceaddc99564736f6c634300081a0033";

    private static String librariesLinkedBinary;

    public static final String FUNC_GETMESSAGEDETAILS = "getMessageDetails";

    public static final String FUNC_GETMESSAGEHASH = "getMessageHash";

    public static final String FUNC_GETSIGNERS = "getSigners";

    public static final String FUNC_HASUSERSIGNED = "hasUserSigned";

    public static final String FUNC_MESSAGECOUNT = "messageCount";

    public static final String FUNC_MESSAGES = "messages";

    public static final String FUNC_SIGNMESSAGE = "signMessage";

    public static final String FUNC_VERIFYMESSAGE = "verifyMessage";

    public static final Event MESSAGESIGNED_EVENT = new Event("MessageSigned", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Utf8String>() {}));
    ;

    public static final Event MESSAGEVERIFIED_EVENT = new Event("MessageVerified", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Utf8String>() {}, new TypeReference<Bytes32>() {}));
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

    public static List<MessageSignedEventResponse> getMessageSignedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(MESSAGESIGNED_EVENT, transactionReceipt);
        ArrayList<MessageSignedEventResponse> responses = new ArrayList<MessageSignedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            MessageSignedEventResponse typedResponse = new MessageSignedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.messageId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.signerUserId = (String) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static MessageSignedEventResponse getMessageSignedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(MESSAGESIGNED_EVENT, log);
        MessageSignedEventResponse typedResponse = new MessageSignedEventResponse();
        typedResponse.log = log;
        typedResponse.messageId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.signerUserId = (String) eventValues.getNonIndexedValues().get(0).getValue();
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

    public static List<MessageVerifiedEventResponse> getMessageVerifiedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(MESSAGEVERIFIED_EVENT, transactionReceipt);
        ArrayList<MessageVerifiedEventResponse> responses = new ArrayList<MessageVerifiedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            MessageVerifiedEventResponse typedResponse = new MessageVerifiedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.messageId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.senderUserId = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.messageHash = (byte[]) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static MessageVerifiedEventResponse getMessageVerifiedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(MESSAGEVERIFIED_EVENT, log);
        MessageVerifiedEventResponse typedResponse = new MessageVerifiedEventResponse();
        typedResponse.log = log;
        typedResponse.messageId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.senderUserId = (String) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.messageHash = (byte[]) eventValues.getNonIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<MessageVerifiedEventResponse> messageVerifiedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getMessageVerifiedEventFromLog(log));
    }

    public Flowable<MessageVerifiedEventResponse> messageVerifiedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(MESSAGEVERIFIED_EVENT));
        return messageVerifiedEventFlowable(filter);
    }

    public RemoteFunctionCall<Tuple4<String, byte[], BigInteger, BigInteger>> getMessageDetails(
            BigInteger messageId) {
        final Function function = new Function(FUNC_GETMESSAGEDETAILS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(messageId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}, new TypeReference<Bytes32>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
        return new RemoteFunctionCall<Tuple4<String, byte[], BigInteger, BigInteger>>(function,
                new Callable<Tuple4<String, byte[], BigInteger, BigInteger>>() {
                    @Override
                    public Tuple4<String, byte[], BigInteger, BigInteger> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple4<String, byte[], BigInteger, BigInteger>(
                                (String) results.get(0).getValue(), 
                                (byte[]) results.get(1).getValue(), 
                                (BigInteger) results.get(2).getValue(), 
                                (BigInteger) results.get(3).getValue());
                    }
                });
    }

    public RemoteFunctionCall<byte[]> getMessageHash(BigInteger messageId) {
        final Function function = new Function(FUNC_GETMESSAGEHASH, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(messageId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}));
        return executeRemoteCallSingleValueReturn(function, byte[].class);
    }

    public RemoteFunctionCall<List> getSigners(BigInteger messageId) {
        final Function function = new Function(FUNC_GETSIGNERS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(messageId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Utf8String>>() {}));
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

    public RemoteFunctionCall<Boolean> hasUserSigned(BigInteger messageId, String userId) {
        final Function function = new Function(FUNC_HASUSERSIGNED, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(messageId), 
                new org.web3j.abi.datatypes.Utf8String(userId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<BigInteger> messageCount() {
        final Function function = new Function(FUNC_MESSAGECOUNT, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<Tuple3<String, byte[], BigInteger>> messages(BigInteger param0) {
        final Function function = new Function(FUNC_MESSAGES, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}, new TypeReference<Bytes32>() {}, new TypeReference<Uint256>() {}));
        return new RemoteFunctionCall<Tuple3<String, byte[], BigInteger>>(function,
                new Callable<Tuple3<String, byte[], BigInteger>>() {
                    @Override
                    public Tuple3<String, byte[], BigInteger> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple3<String, byte[], BigInteger>(
                                (String) results.get(0).getValue(), 
                                (byte[]) results.get(1).getValue(), 
                                (BigInteger) results.get(2).getValue());
                    }
                });
    }

    public RemoteFunctionCall<TransactionReceipt> signMessage(BigInteger messageId,
            String signerUserId) {
        final Function function = new Function(
                FUNC_SIGNMESSAGE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(messageId), 
                new org.web3j.abi.datatypes.Utf8String(signerUserId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> verifyMessage(String senderUserId,
            byte[] messageHash) {
        final Function function = new Function(
                FUNC_VERIFYMESSAGE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(senderUserId), 
                new org.web3j.abi.datatypes.generated.Bytes32(messageHash)), 
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

    public static class MessageSignedEventResponse extends BaseEventResponse {
        public BigInteger messageId;

        public String signerUserId;
    }

    public static class MessageVerifiedEventResponse extends BaseEventResponse {
        public BigInteger messageId;

        public String senderUserId;

        public byte[] messageHash;
    }
}
