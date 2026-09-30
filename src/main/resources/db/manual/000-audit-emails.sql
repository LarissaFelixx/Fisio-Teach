-- Read-only: must return zero rows before applying 001-jwt-security.sql.
SELECT email, COUNT(*) AS accounts, GROUP_CONCAT(subject) AS subjects
FROM (
    SELECT LOWER(TRIM(email)) AS email, CONCAT('ADMIN:', id) AS subject FROM admins
    UNION ALL
    SELECT LOWER(TRIM(email)), CONCAT('PROFISSIONAL:', id) FROM profissionais
    UNION ALL
    SELECT LOWER(TRIM(email)), CONCAT('PACIENTE:', id) FROM pacientes
) identities
GROUP BY email HAVING COUNT(*) > 1;
