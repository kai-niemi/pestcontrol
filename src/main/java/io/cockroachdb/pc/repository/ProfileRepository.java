package io.cockroachdb.pc.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProfileRepository {
    ProfileEntity insertProfileSingleton();

    List<ProfileEntity> insertProfileBatch(int batchSize);

    void updateProfile(ProfileEntity profile);

    void deleteProfileById(UUID id);

    List<ProfileEntity> findAll(int limit);

    Optional<ProfileEntity> findFirst(boolean followerRead);

    Optional<ProfileEntity> findByNextId(UUID id, boolean followerRead);

    Optional<ProfileEntity> findByRandomId();

    void deleteAll();
}
