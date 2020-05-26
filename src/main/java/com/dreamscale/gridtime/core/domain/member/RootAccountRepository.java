package com.dreamscale.gridtime.core.domain.member;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RootAccountRepository extends CrudRepository<RootAccountEntity, UUID> {

    RootAccountEntity findById(UUID id);

    RootAccountEntity findByApiKey(String apiKey);

    RootAccountEntity findByRootEmail(String standarizedEmail);

    RootAccountEntity findByLowerCaseRootUserName(String userName);

    @Modifying
    @Query(nativeQuery = true, value = "update root_account " +
            "set crypt_password = crypt(cast ((:password) as text), gen_salt('bf')) "+
            "where id = (:rootAccountId) ")
    void updatePassword(@Param("rootAccountId") UUID rootAccountId, @Param("password") String password);


    @Query(nativeQuery = true, value = "select * from root_account " +
            "where id = (:rootAccountId) and crypt_password = crypt(cast ((:password) as text), crypt_password)")
    RootAccountEntity checkPasswordAndReturnIfValid(@Param("rootAccountId") UUID rootAccountId, @Param("password") String password);

}
