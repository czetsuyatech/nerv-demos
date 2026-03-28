package com.czetsuyatech.envers.service.impl;

import com.czetsuyatech.envers.persistence.entity.UserEntity;
import com.czetsuyatech.envers.persistence.repository.UserRepository;
import com.czetsuyatech.envers.service.UserService;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionType;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final EntityManager entityManager;

  @Override
  public UserEntity create() {
    return userRepository.save(getUser());
  }

  @Override
  public void update(Long userId) {

    userRepository.findById(userId)
        .map(user -> {
          user.setFirstName("czetsuya");
          user.setLastName("tech");
          return user;
        })
        .map(user -> userRepository.save(user));
  }

  @Override
  public void delete(Long userId) {
    userRepository.deleteById(userId);
  }

  @Override
  public Optional<UserEntity> getById(Long userId) {
    return userRepository.findById(userId);
  }

  @Override
  public Object getHorizontalRevisions(Long userId) {

    AuditReader auditReader = AuditReaderFactory.get(entityManager);

    List<Object[]> revisions = auditReader.createQuery()
        .forRevisionsOfEntity(UserEntity.class, false, true) // false for Object[], true to include deletions
        .getResultList();

    return revisions.stream()
        .map(revision -> {
          UserEntity entity = (UserEntity) revision[0];
          DefaultRevisionEntity revisionEntity = (DefaultRevisionEntity) revision[1];
          RevisionType revisionType = (RevisionType) revision[2];

          return new DefaultRevision(entity, revisionEntity, revisionType);
        }).toList();
  }

  @Override
  public Object getVerticalRevisions(Long userId) {

    AuditReader auditReader = AuditReaderFactory.get(entityManager);

    List<Object[]> revisions = auditReader.createQuery()
        .forRevisionsOfEntity(UserEntity.class, false, true) // false for Object[], true to include deletions
        .getResultList();

    return revisions.stream()
        .map(revision -> {
          UserEntity entity = (UserEntity) revision[0];
          DefaultRevisionEntity revisionEntity = (DefaultRevisionEntity) revision[1];
          RevisionType revisionType = (RevisionType) revision[2];

          return new DefaultRevision(entity, revisionEntity, revisionType);
        }).toList();
  }

  private record DefaultRevision(
      Object entity,
      DefaultRevisionEntity revision,
      RevisionType revisionType
  ) {

  }

  private static UserEntity getUser() {

    return UserEntity.builder()
        .firstName("Edward")
        .lastName("Legaspi")
        .birthDate(LocalDateTime.now())
        .username("czetsuya")
        .hobbies(List.of("chess"))
        .build();
  }
}
