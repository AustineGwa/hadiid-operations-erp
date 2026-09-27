package com.hadiid.erp.customer;

import com.hadiid.erp.customer.dto.CustomerDto;
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
public class CustomerRepository {

    private final JdbcTemplate jdbc;

    public CustomerRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<CustomerDto> MAPPER = (rs, n) -> CustomerDto.builder()
            .id(rs.getLong("id"))
            .name(rs.getString("name"))
            .registrationNo(rs.getString("registration_no"))
            .phone(rs.getString("phone"))
            .address(rs.getString("address"))
            .jobCount(rs.getInt("job_count"))
            .build();

    private static final String BASE_SELECT = """
            SELECT c.id, c.name, c.registration_no, c.phone, c.address,
                   (SELECT COUNT(*) FROM fabrication_jobs j WHERE j.customer_id = c.id) AS job_count
            FROM customers c
            """;

    public List<CustomerDto> search(String query, int limit, int offset) {
        if (query == null || query.isBlank()) {
            return jdbc.query(BASE_SELECT + " ORDER BY c.name LIMIT ? OFFSET ?", MAPPER, limit, offset);
        }
        String like = "%" + query.trim() + "%";
        return jdbc.query(BASE_SELECT + """
                 WHERE c.name LIKE ? OR c.registration_no LIKE ? OR c.phone LIKE ?
                 ORDER BY c.name LIMIT ? OFFSET ?
                """, MAPPER, like, like, like, limit, offset);
    }

    public long countSearch(String query) {
        if (query == null || query.isBlank()) {
            return jdbc.queryForObject("SELECT COUNT(*) FROM customers", Long.class);
        }
        String like = "%" + query.trim() + "%";
        return jdbc.queryForObject("""
                SELECT COUNT(*) FROM customers WHERE name LIKE ? OR registration_no LIKE ? OR phone LIKE ?
                """, Long.class, like, like, like);
    }

    public Optional<CustomerDto> findById(Long id) {
        return jdbc.query(BASE_SELECT + " WHERE c.id = ?", MAPPER, id).stream().findFirst();
    }

    /** Existing customer by registration number, for job-intake lookup-or-create (§H.8). */
    public Optional<CustomerDto> findByRegistrationNo(String registrationNo) {
        if (registrationNo == null || registrationNo.isBlank()) return Optional.empty();
        return jdbc.query(BASE_SELECT + " WHERE c.registration_no = ?", MAPPER, registrationNo).stream().findFirst();
    }

    public Long insert(String name, String registrationNo, String phone, String address) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO customers (name, registration_no, phone, address) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);
            ps.setString(2, registrationNo);
            ps.setString(3, phone);
            ps.setString(4, address);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public void update(Long id, String name, String phone, String address) {
        jdbc.update("UPDATE customers SET name = ?, phone = ?, address = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                name, phone, address, id);
    }
}
