-- MIGRAÇÃO MANUAL — execute uma única vez em bancos antigos.
-- NÃO é carregada automaticamente pelo Spring.
-- Converte apenas colunas que ainda são TIMESTAMP WITHOUT TIME ZONE.
-- Os valores existentes são interpretados no fuso America/Maceio.

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'solicitacao'
          AND column_name = 'data_criacao' AND data_type = 'timestamp without time zone'
    ) THEN
        ALTER TABLE solicitacao ALTER COLUMN data_criacao TYPE TIMESTAMP WITH TIME ZONE
            USING data_criacao AT TIME ZONE 'America/Maceio';
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'solicitacao'
          AND column_name = 'data_agendamento' AND data_type = 'timestamp without time zone'
    ) THEN
        ALTER TABLE solicitacao ALTER COLUMN data_agendamento TYPE TIMESTAMP WITH TIME ZONE
            USING data_agendamento AT TIME ZONE 'America/Maceio';
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'solicitacao'
          AND column_name = 'data_atualizacao' AND data_type = 'timestamp without time zone'
    ) THEN
        ALTER TABLE solicitacao ALTER COLUMN data_atualizacao TYPE TIMESTAMP WITH TIME ZONE
            USING data_atualizacao AT TIME ZONE 'America/Maceio';
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'historico'
          AND column_name = 'data_hora' AND data_type = 'timestamp without time zone'
    ) THEN
        ALTER TABLE historico ALTER COLUMN data_hora TYPE TIMESTAMP WITH TIME ZONE
            USING data_hora AT TIME ZONE 'America/Maceio';
    END IF;
END $$;
