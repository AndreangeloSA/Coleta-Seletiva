ALTER TABLE ponto_coleta ADD COLUMN IF NOT EXISTS municipio VARCHAR(255), ADD COLUMN IF NOT EXISTS bairro VARCHAR(255);
CREATE INDEX IF NOT EXISTS ix_solicitacao_ponto_data ON solicitacao(ponto_id, data_criacao);
