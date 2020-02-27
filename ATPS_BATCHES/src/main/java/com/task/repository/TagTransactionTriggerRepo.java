package com.task.repository;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.task.entity.TagTransactiontriggersEntity;

public interface TagTransactionTriggerRepo extends JpaRepository<TagTransactiontriggersEntity,Serializable> {

	@Query(value="from TagTransactiontriggersEntity where transactionstatus=:status and tranxFailReason=:reason and createDt=:date and reminderMessage=:reminderMessageSwitch")
	 public List<TagTransactiontriggersEntity> getTagTriggerTransactionRecords(String status,String reason,Date date,String  reminderMessageSwitch);
	
	
	@Modifying
	@Transactional 
	@Query(value="update TagTransactiontriggersEntity entity set entity.reminderMessage =:messageSwitch where entity.tagId =:tagId")
	public Integer updateMessageSwitch(Integer tagId,String messageSwitch);
}
