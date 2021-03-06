package by.epam.clinic.core.service.impl;

import by.epam.clinic.core.model.Customer;
import by.epam.clinic.core.model.User;
import by.epam.clinic.core.pool.TransactionManager;
import by.epam.clinic.core.pool.TransactionManagerException;
import by.epam.clinic.core.repository.impl.*;
import by.epam.clinic.core.service.CustomerService;
import by.epam.clinic.core.specification.impl.FindUserByLoginAndPasswordSpecification;
import by.epam.clinic.core.specification.impl.FindUserByLoginSpecification;
import by.epam.clinic.util.TextEncryptor;
import by.epam.clinic.util.TextEncryptorException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;


public class CustomerServiceImpl implements CustomerService {
    private static Logger logger = LogManager.getLogger();

    public boolean createCustomer(User user, Customer customer) throws ServiceException {
        TransactionManager transactionManager = new TransactionManager();
        try {
            transactionManager.init();
            transactionManager.beginTransaction();
            UserRepository userRepository = new UserRepository();
            CustomerRepository customerRepository = new CustomerRepository();
            transactionManager.setConnectionToRepository(customerRepository , userRepository);
            String unencryptedPassword = user.getPassword();
            user.setPassword(TextEncryptor.encrypt(unencryptedPassword));
            FindUserByLoginSpecification specification =
                    new FindUserByLoginSpecification(user.getLogin());
            List<User> userList = userRepository.query(specification);
            if(userList.size() == 0) {
                userRepository.add(user);
                customer.setUserId(user.getId());
                customerRepository.add(customer);
                transactionManager.commitTransaction();
            } else {
                return false;
            }
        } catch (TransactionManagerException e) {
            throw new ServiceException("Transaction manager error",e);
        } catch (RepositoryException e) {
            throw new ServiceException(e.getMessage());
        } catch (TextEncryptorException e) {
           throw new ServiceException("Password encrypting error",e);
        } finally {
            try {
                transactionManager.releaseResources();
            } catch (TransactionManagerException e) {
                logger.error("Error in releasing connection", e);
            }
        }
        return true;
    }
}
