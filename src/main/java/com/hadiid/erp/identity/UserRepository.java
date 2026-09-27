package com.hadiid.erp.identity;

import com.hadiid.erp.identity.dto.AuthUserRecord;
import com.hadiid.erp.identity.dto.UserCreateRequest;
import com.hadiid.erp.identity.dto.UserDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbc;

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<AuthUserRecord> AUTH_MAPPER = (rs, rowNum) -> AuthUserRecord.builder()
            .id(rs.getLong("id"))
            .username(rs.getString("username"))
            .fullName(rs.getString("full_name"))
            .passwordHash(rs.getString("password_hash"))
            .roleCode(rs.getString("role_code"))
            .status(rs.getString("status"))
            .build();

    private static final RowMapper<UserDto> DTO_MAPPER = (rs, rowNum) -> UserDto.builder()
            .id(rs.getLong("id"))
            .username(rs.getString("username"))
            .fullName(rs.getString("full_name"))
            .email(rs.getString("email"))
            .phone(rs.getString("phone"))
            .roleCode(rs.getString("role_code"))
            .roleLabel(rs.getString("role_label"))
            .status(rs.getString("status"))
            .team(rs.getString("team"))
            .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
            .build();

    public Optional<AuthUserRecord> findAuthRecordByUsername(String username) {
        String sql = """
                SELECT u.id, u.username, u.full_name, u.password_hash, u.status, r.code AS role_code
                FROM users u JOIN roles r ON r.id = u.role_id
                WHERE u.username = ?
                """;
        return jdbc.query(sql, AUTH_MAPPER, username).stream().findFirst();
    }

    public List<String> findPermissionCodesForRole(String roleCode) {
        String sql = """
                SELECT p.code
                FROM permissions p
                JOIN role_permissions rp ON rp.permission_id = p.id
                JOIN roles r ON r.id = rp.role_id
                WHERE r.code = ?
                """;
        return jdbc.queryForList(sql, String.class, roleCode);
    }

    public List<UserDto> findAll() {
        String sql = """
                SELECT u.id, u.username, u.full_name, u.email, u.phone, u.status, u.team, u.created_at,
                       r.code AS role_code, r.label AS role_label
                FROM users u JOIN roles r ON r.id = u.role_id
                ORDER BY u.full_name
                """;
        return jdbc.query(sql, DTO_MAPPER);
    }

    public Optional<UserDto> findById(Long id) {
        String sql = """
                SELECT u.id, u.username, u.full_name, u.email, u.phone, u.status, u.team, u.created_at,
                       r.code AS role_code, r.label AS role_label
                FROM users u JOIN roles r ON r.id = u.role_id
                WHERE u.id = ?
                """;
        return jdbc.query(sql, DTO_MAPPER, id).stream().findFirst();
    }

    public Long insert(UserCreateRequest req, String passwordHash) {
        String sql = """
                INSERT INTO users (username, full_name, email, phone, password_hash, role_id, status, team)
                VALUES (?, ?, ?, ?, ?, ?, 'ACTIVE', ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, req.getUsername());
            ps.setString(2, req.getFullName());
            ps.setString(3, req.getEmail());
            ps.setString(4, req.getPhone());
            ps.setString(5, passwordHash);
            ps.setInt(6, req.getRoleId());
            ps.setString(7, req.getTeam());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public void updateStatus(Long id, String status) {
        jdbc.update("UPDATE users SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?", status, id);
    }

    public void updateProfile(Long id, String fullName, String email, String phone, Integer roleId, String team) {
        jdbc.update("""
                UPDATE users SET full_name = ?, email = ?, phone = ?, role_id = ?, team = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """, fullName, email, phone, roleId, team, id);
    }
}
