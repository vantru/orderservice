package com.appsdeveloperblog.estore.ordersservice.grpcclientservice;

import org.springframework.stereotype.Service;

import com.estore.user.grpc.GetUserRequest;
import com.estore.user.grpc.UserResponse;
import com.estore.user.grpc.UserServiceGrpc;

import net.devh.boot.grpc.client.inject.GrpcClient;

@Service
public class UserGrpcClientService {
    
    @GrpcClient("user-service")
    private UserServiceGrpc.UserServiceBlockingStub userStub;
    public UserResponse getUser(String userID){
         GetUserRequest userRequest = null;
        try{
            userRequest = GetUserRequest.newBuilder().setUserId(userID).build();
        }
        catch(Exception ex){
            System.err.println("error grpc : " + ex.getMessage());
            return null;
        }
        return userStub.getUser(userRequest);
    }
    
}
