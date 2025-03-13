package com.status_app.auth_service.repository;

import com.status_app.auth_service.entity.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface IUserRepository extends MongoRepository<User, ObjectId> {
    User findByUsername(String username);
    User findByEmail(String email);
}
