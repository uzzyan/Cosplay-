UPDATE sys_user SET password='$2a$10$SAhZtBAKXT1n/8pGj405KO1C5dcWURUovOkwbMVB6In26kkIeOU.m' WHERE username='admin';
SELECT id, username, role, password FROM sys_user WHERE username='admin';
