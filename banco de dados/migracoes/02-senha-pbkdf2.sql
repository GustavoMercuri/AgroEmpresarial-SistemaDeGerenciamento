-- Refatoração: senhas com PBKDF2.
-- O novo formato (pbkdf2$iteracoes$salt$hash) tem cerca de 90 caracteres
-- e não cabe no varchar(40) original.
-- As senhas MD5 existentes continuam válidas e são convertidas no próximo login.
USE agroempresarial;
ALTER TABLE funcionario MODIFY senha VARCHAR(120);
