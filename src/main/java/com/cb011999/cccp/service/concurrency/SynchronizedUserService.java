package com.cb011999.cccp.service.concurrency;

import com.cb011999.cccp.domain.model.Employee;
import com.cb011999.cccp.domain.model.OnlineCustomer;
import com.cb011999.cccp.service.UserService;
import com.cb011999.cccp.service.impl.UserServiceImpl;
import com.cb011999.cccp.web.concurrency.RequestQueue;
import com.cb011999.cccp.web.concurrency.Task;

import java.util.List;
import java.util.Optional;

public class SynchronizedUserService implements UserService {

    private final UserServiceImpl delegate;
    private final RequestQueue requestQueue;
    private final Object userLock = new Object();

    public SynchronizedUserService(UserServiceImpl delegate) {
        this.delegate = delegate;
        this.requestQueue = RequestQueue.getInstance();
    }

    @Override
    public RegistrationResult registerCustomer(String name, String contact, String email,
                                               String password, String address) {
        // Registration modifies the user repository — queue it
        final Task[] holder = new Task[1];
        holder[0] = new Task(() -> {
            synchronized (userLock) {
                RegistrationResult result = delegate.registerCustomer(name, contact, email, password, address);
                holder[0].setResult(result);
            }
        });
        requestQueue.submitTask(holder[0]);

        if (holder[0].hasError()) {
            return new RegistrationResult(false, "Server error: " + holder[0].getError(), null);
        }
        return (RegistrationResult) holder[0].getResult();
    }


    @Override
    public LoginResult loginWithEmail(String email, String password) {
        synchronized (userLock) {
            return delegate.loginWithEmail(email, password);
        }
    }

    @Override
    public List<OnlineCustomer> getAllCustomers() {
        synchronized (userLock) {
            return delegate.getAllCustomers();
        }
    }

    @Override
    public List<Employee> getAllEmployees() {
        synchronized (userLock) {
            return delegate.getAllEmployees();
        }
    }

    @Override
    public Optional<Employee> findEmployeeByNumber(String employeeNumber) {
        synchronized (userLock) {
            return delegate.findEmployeeByNumber(employeeNumber);
        }
    }
}