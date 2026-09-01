import { authGuard } from './guards/auth-guard';
import { routes } from './app.routes';

describe('rotas administrativas de artistas', () => {
  const caminhos = [
    'admin/banco/artistas',
    'admin/banco/artistas/novo',
    'admin/banco/artistas/:id/editar'
  ];

  for (const caminho of caminhos) {
    it(`deve proteger a rota ${caminho} para ADMIN`, () => {
      const rota = routes.find(item => item.path === caminho);

      expect(rota).toBeDefined();
      expect(rota?.loadComponent).toBeDefined();
      expect(rota?.canActivate).toContain(authGuard);
      expect(rota?.data?.['expectedRole']).toBe('ADMIN');
    });
  }
});

describe('rotas administrativas de álbuns', () => {
  const caminhos = [
    'admin/banco/albuns',
    'admin/banco/albuns/novo',
    'admin/banco/albuns/:id/editar'
  ];

  for (const caminho of caminhos) {
    it(`deve proteger a rota ${caminho} para ADMIN`, () => {
      const rota = routes.find(item => item.path === caminho);

      expect(rota).toBeDefined();
      expect(rota?.loadComponent).toBeDefined();
      expect(rota?.canActivate).toContain(authGuard);
      expect(rota?.data?.['expectedRole']).toBe('ADMIN');
    });
  }
});

describe('rotas administrativas de músicas', () => {
  const caminhos = [
    'admin/banco/musicas',
    'admin/banco/musicas/nova',
    'admin/banco/musicas/:id/editar'
  ];

  for (const caminho of caminhos) {
    it(`deve proteger a rota ${caminho} para ADMIN`, () => {
      const rota = routes.find(item => item.path === caminho);

      expect(rota).toBeDefined();
      expect(rota?.loadComponent).toBeDefined();
      expect(rota?.canActivate).toContain(authGuard);
      expect(rota?.data?.['expectedRole']).toBe('ADMIN');
    });
  }
});

describe('rota administrativa de relatórios', () => {
  it('deve carregar a página e exigir perfil ADMIN', () => {
    const rota = routes.find(
      item => item.path === 'admin/relatorios'
    );

    expect(rota).toBeDefined();
    expect(rota?.loadComponent).toBeDefined();
    expect(rota?.canActivate).toContain(authGuard);
    expect(rota?.data?.['expectedRole']).toBe('ADMIN');
  });
});

describe('rota de detalhes do artista', () => {
  it('deve carregar a página e exigir autenticação', () => {
    const rota = routes.find(
      item => item.path === 'artistas/:id'
    );

    expect(rota).toBeDefined();
    expect(rota?.loadComponent).toBeDefined();
    expect(rota?.canActivate).toContain(authGuard);
  });
});

describe('rota de detalhes do álbum', () => {
  it('deve carregar a página e exigir autenticação', () => {
    const rota = routes.find(
      item => item.path === 'albuns/:id'
    );

    expect(rota).toBeDefined();
    expect(rota?.loadComponent).toBeDefined();
    expect(rota?.canActivate).toContain(authGuard);
  });
});

describe('carregamento das rotas com loadComponent', () => {
  it('deve resolver todos os componentes carregados sob demanda', async () => {
    const rotasComLoadComponent = routes.filter(
      rota => rota.loadComponent
    );

    expect(rotasComLoadComponent.length).toBeGreaterThan(0);

    const componentes = await Promise.all(
      rotasComLoadComponent.map(rota => rota.loadComponent!())
    );

    for (const componente of componentes) {
      expect(typeof componente).toBe('function');
    }
  }, 20000);
});

describe('rota raiz e curinga', () => {
  it('deve redirecionar a raiz para o login', () => {
    const raiz = routes.find(item => item.path === '');

    expect(raiz?.redirectTo).toBe('login');
    expect(raiz?.pathMatch).toBe('full');
  });

  it('deve redirecionar rotas desconhecidas para o login', () => {
    const coringa = routes.find(item => item.path === '**');

    expect(coringa?.redirectTo).toBe('login');
  });
});
