export default {
  forbidden: [
    {
      name: 'customers-domain-stays-pure',
      severity: 'error',
      comment:
        'Customers domain code must not depend on application, infrastructure, state, or UI details. Keep the domain model isolated.',
      from: { path: '^src/modules/customers/domain' },
      to: {
        path: '^src/modules/customers/(application|infrastructure|state|ui|mappers)',
      },
    },
    {
      name: 'customers-application-uses-ports-only',
      severity: 'error',
      comment:
        'Customers application services must only depend on the domain (and shared kernel), not on infrastructure or UI modules.',
      from: { path: '^src/modules/customers/application' },
      to: {
        path: '^src/modules/customers/(infrastructure|ui|state|mappers)',
      },
    },
    {
      name: 'customers-infrastructure-isolation',
      severity: 'error',
      comment:
        'Infrastructure adapters may not import UI modules; UI flows must go through application services (ports).',
      from: { path: '^src/modules/customers/infrastructure' },
      to: { path: '^src/modules/customers/ui' },
    },
  ],
  options: {
    doNotFollow: {
      path: 'node_modules',
    },
    tsPreCompilationDeps: true,
    combinedDependencies: true,
    enhancedResolveOptions: {
      extensions: ['.ts', '.tsx', '.js', '.vue'],
    },
  },
};
