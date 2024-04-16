
INSERT INTO curso (id, nome_curso)
SELECT dados.id, dados.nome_curso
FROM (
  SELECT '10' AS id, 'Análise e Desenvolvimento de Sistemas' AS nome_curso
  UNION ALL
  SELECT '11', 'Letras Português e Espanhol'
  UNION ALL
  SELECT '12', 'Eletrônica Industrial'
  UNION ALL
  SELECT '13', 'Gestão Desportiva e de Lazer'
  UNION ALL
  SELECT '14', 'Processos Gerenciais'
  UNION ALL
  SELECT '15', 'Setor Estágio'
  UNION ALL
  SELECT '16', 'Diretor'
  UNION ALL
  SELECT '17', 'Lazer'
  UNION ALL
  SELECT '18', 'Informática'
  UNION ALL
  SELECT '19', 'Eletrônica'
  UNION ALL
  SELECT '20', 'Guia de Turismo'
) AS dados
WHERE NOT EXISTS (
  SELECT 1
  FROM curso
  WHERE curso.id = dados.id
    AND curso.nome_curso = dados.nome_curso
);


INSERT INTO roles (id,name)
SELECT dados.id, dados.name
FROM (
         SELECT '1' AS id, 'ROLE_ALUNO' AS name
         UNION ALL
         SELECT '2', 'ROLE_SERVIDOR'
         UNION ALL
         SELECT '3', 'ROLE_SESTAGIO'
         UNION ALL
         SELECT '4', 'ROLE_DIRETOR'
     ) AS dados
WHERE NOT EXISTS (
        SELECT 1
        FROM roles
        WHERE roles.id = dados.id
          AND roles.name = dados.name
    );

INSERT INTO usuarios (email, senha, roles_id)
SELECT 'admin@restinga.ifrs.edu.br', '$2a$12$6EloNIdl/tEaAgMdUuklMeHBPlQf7Ub5d8Bu9aXdbcC5wkAbBFjAi', '3'
FROM (
         SELECT 'admin@restinga.ifrs.edu.br' AS email
     ) AS dados
WHERE NOT EXISTS (
    SELECT 1
    FROM usuarios
    WHERE usuarios.email = dados.email
);


INSERT INTO servidores (cargo, nome, curso_id, role_id, usuario_sistema_id)
SELECT 'Setor estágio' AS cargo, 'admin' AS nome, 15 AS curso_id, 3 AS role_id, 1 AS usuario_sistema_id
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM servidores
    WHERE
          servidores.cargo = 'Setor estágio'
      AND servidores.nome = 'admin'
);
