-- DEMONSTRACAO: EXECEUTAR NUM BANCO DE DADOS

BEGIN;
DO $$
DECLARE
  morador_demo bigint;
  ponto_demo bigint;
  operador_demo bigint;
BEGIN
  INSERT INTO morador(nome,email) VALUES ('Carla','carla@example.invalid')
    ON CONFLICT(email) DO NOTHING;
  SELECT id INTO morador_demo FROM morador WHERE email='carla@example.invalid';
  IF NOT EXISTS (SELECT 1 FROM endereco WHERE morador_id=morador_demo AND logradouro='Rua das Mangueiras') THEN
    INSERT INTO endereco(morador_id,logradouro,numero,bairro,municipio,uf,cep)
    VALUES(morador_demo,'Rua Principal','10','Centro','Lagarto','SE','49400000');
  END IF;
  INSERT INTO material(nome,unidade_medida,orientacao_preparo) VALUES
    ('Papel demo','kg','Separar limpo e seco.'),
    ('Plástico demo','kg','Esvaziar e enxaguar.'),
    ('Vidro demo','un','Embalar peças sem quebrar.')
    ON CONFLICT(nome) DO NOTHING;
  SELECT id INTO ponto_demo FROM ponto_coleta WHERE nome='Cooperativa Ipê (demo)' ORDER BY id LIMIT 1;
  IF ponto_demo IS NULL THEN
    INSERT INTO ponto_coleta(nome,endereco,municipio,bairro,horarios_funcionamento,recolhimento_domiciliar)
    VALUES('Cooperativa Ipê (demo)','Rua Secundária, 20','Aracaju','Farolandia','Segunda a sexta, 8h às 17h',true)
    RETURNING id INTO ponto_demo;
  END IF;
  INSERT INTO material_aceito(ponto_id,material_id,limite_por_solicitacao)
    SELECT ponto_demo,id,50 FROM material WHERE nome IN ('Papel demo','Plástico demo')
    ON CONFLICT(ponto_id,material_id) DO NOTHING;
  INSERT INTO operador(nome,email) VALUES('Julio Demo','julio@example.invalid') ON CONFLICT(email) DO NOTHING;
  SELECT id INTO operador_demo FROM operador WHERE email='julio@example.invalid';
  INSERT INTO operador_ponto(operador_id,ponto_id) VALUES(operador_demo,ponto_demo)
    ON CONFLICT(operador_id,ponto_id) DO NOTHING;
END $$;
COMMIT;
SELECT id AS morador_para_demonstracao FROM morador WHERE email='carla@example.invalid';
