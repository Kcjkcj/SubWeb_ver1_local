package com.kcj.SubWeb.repository;

import com.kcj.SubWeb.entity.Role;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;

public interface RoleRepository extends CrudRepository <Role,Integer> {
    //Set<Role> findByAccount_AccountId(int id); //외래키를 사용하는 경우 참조하는 것의 엔티티와 속성을 적어주면 됨
}
