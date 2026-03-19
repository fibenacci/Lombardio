package io.lombardio.identityaccess.support;

import io.lombardio.identityaccess.access.domain.Permission;
import io.lombardio.identityaccess.access.domain.PermissionRepository;
import io.lombardio.identityaccess.access.domain.Branch;
import io.lombardio.identityaccess.access.domain.BranchRepository;
import io.lombardio.identityaccess.access.domain.Role;
import io.lombardio.identityaccess.access.domain.RoleRepository;
import io.lombardio.identityaccess.access.domain.User;
import io.lombardio.identityaccess.access.domain.UserRepository;
import io.lombardio.identityaccess.auth.domain.MfaChallenge;
import io.lombardio.identityaccess.auth.domain.MfaChallengeRepository;
import io.lombardio.identityaccess.auth.domain.SessionToken;
import io.lombardio.identityaccess.auth.domain.SessionTokenRepository;
import io.lombardio.identityaccess.auth.domain.TotpCredential;
import io.lombardio.identityaccess.auth.domain.TotpCredentialRepository;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class InMemoryRepositories {

    private InMemoryRepositories() {
    }

    public static final class Users implements UserRepository {
        private final Map<String, User> store = new LinkedHashMap<>();

        @Override
        public List<User> findAll() {
            return store.values().stream().sorted(Comparator.comparing(User::createdAt)).toList();
        }

        @Override
        public Optional<User> findById(String id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public Optional<User> findByEmail(String email) {
            return store.values().stream().filter(user -> user.email().equalsIgnoreCase(email)).findFirst();
        }

        @Override
        public Optional<User> findByTenantIdAndEmail(String tenantId, String email) {
            return store.values().stream()
                    .filter(user -> user.tenantId().equals(tenantId))
                    .filter(user -> user.email().equalsIgnoreCase(email))
                    .findFirst();
        }

        @Override
        public User save(User user) {
            store.put(user.id(), user);
            return user;
        }
    }

    public static final class Roles implements RoleRepository {
        private final Map<String, Role> store = new LinkedHashMap<>();

        @Override
        public List<Role> findAll() {
            return store.values().stream().toList();
        }

        @Override
        public Optional<Role> findById(String id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public Optional<Role> findByKey(String key) {
            return store.values().stream().filter(role -> role.key().equals(key)).findFirst();
        }

        @Override
        public Optional<Role> findByTenantIdAndKey(String tenantId, String key) {
            return store.values().stream()
                    .filter(role -> role.tenantId().equals(tenantId))
                    .filter(role -> role.key().equals(key))
                    .findFirst();
        }

        @Override
        public Role save(Role role) {
            store.put(role.id(), role);
            return role;
        }
    }

    public static final class Branches implements BranchRepository {
        private final Map<String, Branch> store = new LinkedHashMap<>();

        @Override
        public List<Branch> findAll() {
            return store.values().stream().toList();
        }

        @Override
        public Optional<Branch> findById(String id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public Optional<Branch> findByTenantIdAndKey(String tenantId, String key) {
            return store.values().stream()
                    .filter(branch -> branch.tenantId().equals(tenantId))
                    .filter(branch -> branch.key().equals(key))
                    .findFirst();
        }

        @Override
        public Branch save(Branch branch) {
            store.put(branch.id(), branch);
            return branch;
        }
    }

    public static final class Permissions implements PermissionRepository {
        private final Map<String, Permission> store = new LinkedHashMap<>();

        @Override
        public List<Permission> findAll() {
            return store.values().stream().toList();
        }

        @Override
        public Optional<Permission> findByKey(String key) {
            return Optional.ofNullable(store.get(key));
        }

        @Override
        public Permission save(Permission permission) {
            store.put(permission.key(), permission);
            return permission;
        }
    }

    public static final class Sessions implements SessionTokenRepository {
        private final Map<String, SessionToken> store = new LinkedHashMap<>();

        @Override
        public SessionToken save(SessionToken sessionToken) {
            store.put(sessionToken.token(), sessionToken);
            return sessionToken;
        }

        @Override
        public Optional<SessionToken> findByToken(String token) {
            return Optional.ofNullable(store.get(token));
        }

        @Override
        public void deleteByToken(String token) {
            store.remove(token);
        }

        @Override
        public void deleteByUserId(String userId) {
            store.entrySet().removeIf(entry -> entry.getValue().userId().equals(userId));
        }

        public int size() {
            return store.size();
        }
    }

    public static final class TotpCredentials implements TotpCredentialRepository {
        private final Map<String, TotpCredential> store = new LinkedHashMap<>();

        @Override
        public Optional<TotpCredential> findByUserId(String userId) {
            return Optional.ofNullable(store.get(userId));
        }

        @Override
        public TotpCredential save(TotpCredential credential) {
            store.put(credential.userId(), credential);
            return credential;
        }

        @Override
        public void deleteByUserId(String userId) {
            store.remove(userId);
        }
    }

    public static final class Challenges implements MfaChallengeRepository {
        private final Map<String, MfaChallenge> store = new LinkedHashMap<>();

        @Override
        public Optional<MfaChallenge> findById(String id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public MfaChallenge save(MfaChallenge challenge) {
            store.put(challenge.id(), challenge);
            return challenge;
        }

        @Override
        public void deleteById(String id) {
            store.remove(id);
        }

        @Override
        public void deleteByUserId(String userId) {
            store.entrySet().removeIf(entry -> entry.getValue().userId().equals(userId));
        }

        public int size() {
            return store.size();
        }
    }
}
