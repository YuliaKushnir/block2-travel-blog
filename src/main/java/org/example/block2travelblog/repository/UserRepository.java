package org.example.block2travelblog.repository;

import org.example.block2travelblog.data.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for user data access.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user for the given email
     *
     * @param email user email
     * @return optional user
     */
    Optional<User> findByEmail(String email);

}
