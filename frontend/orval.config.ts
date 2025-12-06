import { defineConfig } from 'orval';

export default defineConfig({
  api: {
    input: {
      target: process.env.API_SCHEMA_URL || 'http://localhost:8080/v3/api-docs',
    },
    output: {
      mode: 'tags-split',
      target: './src/lib/api/endpoints',
      schemas: './src/lib/api/models',
      client: 'react-query',
      override: {
        mutator: {
          path: './src/lib/api/axios-instance.ts',
          name: 'customAxiosInstance',
        },
      },
      clean: true,
    },
    hooks: {
      afterAllFilesWrite: 'prettier --write',
    },
  },
});