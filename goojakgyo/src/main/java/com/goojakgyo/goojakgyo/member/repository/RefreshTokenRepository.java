package com.goojakgyo.goojakgyo.member.repository;

import com.goojakgyo.goojakgyo.member.domain.RefreshToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {

}
