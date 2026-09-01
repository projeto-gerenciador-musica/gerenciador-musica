import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpErrorResponse, provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { ActivatedRoute, convertToParamMap, provideRouter, Router } from '@angular/router';
import { throwError } from 'rxjs';
import { PlaylistDetalhe } from './playlist-detalhe';
import { PlaylistResponse } from '../../models/PlaylistResponse';
import { PlaylistService } from '../../services/playlist';
import { vi } from 'vitest';

describe('PlaylistDetalhe', () => {
  let component: PlaylistDetalhe;
  let fixture: ComponentFixture<PlaylistDetalhe>;
  let httpMock: HttpTestingController;
  let router: Router;
  const apiUrl = 'http://localhost:8080/api/playlists';

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PlaylistDetalhe],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: { paramMap: convertToParamMap({ id: '10' }) },
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PlaylistDetalhe);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
  });

  afterEach(() => {
    httpMock.verify();
    vi.restoreAllMocks(); // Limpa os mocks do Vitest
  });

  function playlistComUmaMusica(): PlaylistResponse {
    return {
      id: 10,
      nome: 'Favoritas',
      descricao: '',
      capaUrl: 'https://exemplo.com/capa.jpg',
      musicas: [{
        id: 5, titulo: 'Bohemian Rhapsody', artista: 'Queen',
        capaUrl: 'https://exemplo.com/capa-musica.jpg'
      }],
      especial: false,
    };
  }

  it('deve carregar a playlist ao iniciar', () => {
    fixture.detectChanges();
    httpMock.expectOne(`${apiUrl}/10`).flush(playlistComUmaMusica());
    expect(component.playlist?.nome).toBe('Favoritas');
    expect(component.carregando).toBe(false);
  });

  it('deve informar erro inesperado ao carregar a playlist', () => {
    vi.spyOn(console, 'error').mockImplementation(() => {});

    fixture.detectChanges();

    httpMock
      .expectOne(`${apiUrl}/10`)
      .flush(
        {},
        {
          status: 500,
          statusText: 'Internal Server Error'
        }
      );

    expect(component.mensagemErro).toBe(
      'Não foi possível carregar a playlist. Tente novamente mais tarde.'
    );
    expect(component.carregando).toBe(false);
  });

  it('deve remover a música da lista local quando a remoção tem sucesso', () => {
    // Mocka o confirm para retornar 'true' (clicar em OK)
    vi.spyOn(window, 'confirm').mockReturnValue(true);

    fixture.detectChanges();
    httpMock.expectOne(`${apiUrl}/10`).flush(playlistComUmaMusica());

    component.removerMusica(5);

    const requisicao = httpMock.expectOne(`${apiUrl}/10/musicas/5`);
    expect(requisicao.request.method).toBe('DELETE');
    requisicao.flush(null, { status: 204, statusText: 'No Content' });

    expect(component.playlist?.musicas).toHaveLength(0);
    expect(component.removendoMusicaId).toBeNull();
  });

  it('não deve permitir clicar em remover duas vezes para a mesma música', () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);

    fixture.detectChanges();
    httpMock.expectOne(`${apiUrl}/10`).flush(playlistComUmaMusica());

    component.removerMusica(5);
    component.removerMusica(5);

    const requisicao = httpMock.expectOne(`${apiUrl}/10/musicas/5`);
    expect(requisicao.request.method).toBe('DELETE');
    requisicao.flush(null, { status: 204, statusText: 'No Content' });
  });

  it('deve mostrar mensagem de erro quando a remoção falha', () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);

    fixture.detectChanges();
    httpMock.expectOne(`${apiUrl}/10`).flush(playlistComUmaMusica());

    component.removerMusica(5);

    httpMock
      .expectOne(`${apiUrl}/10/musicas/5`)
      .flush({ message: 'erro' }, { status: 500, statusText: 'Internal Server Error' });

    expect(component.mensagemErro).toBeTruthy();
    expect(component.playlist?.musicas).toHaveLength(1);
  });

  it('deve exibir a capa da playlist e o link para edição', () => {
    fixture.detectChanges();
    httpMock.expectOne(`${apiUrl}/10`).flush(playlistComUmaMusica());
    fixture.detectChanges();

    const capa = fixture.nativeElement.querySelector('.capa img');
    expect(capa.getAttribute('src')).toBe('https://exemplo.com/capa.jpg');

    const linkEditar = fixture.nativeElement.querySelector(
      'a[href="/playlists/10/editar"]'
    );
    expect(linkEditar).not.toBeNull();

    const capaMusica = fixture.nativeElement.querySelector('.musica-capa img');
    expect(capaMusica.getAttribute('src')).toBe('https://exemplo.com/capa-musica.jpg');
  });

  it('deve excluir a playlist quando confirmado e navegar para a listagem', () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    const navigateSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);

    fixture.detectChanges();
    httpMock.expectOne(`${apiUrl}/10`).flush(playlistComUmaMusica());

    component.excluirPlaylist();

    const requisicao = httpMock.expectOne(`${apiUrl}/10`);
    expect(requisicao.request.method).toBe('DELETE');
    requisicao.flush(null, { status: 204, statusText: 'No Content' });

    expect(navigateSpy).toHaveBeenCalledWith(['/playlists']);
  });

  it('não deve excluir a playlist quando o usuário cancela a confirmação', () => {
    vi.spyOn(window, 'confirm').mockReturnValue(false);

    fixture.detectChanges();
    httpMock.expectOne(`${apiUrl}/10`).flush(playlistComUmaMusica());

    component.excluirPlaylist();

    httpMock.expectNone(`${apiUrl}/10`);
    expect(component.excluindo).toBe(false);
  });

  it('deve mostrar mensagem de erro quando a exclusão falha', () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);

    fixture.detectChanges();
    httpMock.expectOne(`${apiUrl}/10`).flush(playlistComUmaMusica());

    component.excluirPlaylist();

    httpMock
      .expectOne(`${apiUrl}/10`)
      .flush({ message: 'erro' }, { status: 500, statusText: 'Internal Server Error' });

    expect(component.mensagemErro).toBeTruthy();
    expect(component.excluindo).toBe(false);
  });

  it('não deve excluir novamente enquanto uma exclusão está em andamento', () => {
    const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);

    fixture.detectChanges();
    httpMock.expectOne(`${apiUrl}/10`).flush(playlistComUmaMusica());

    component.excluirPlaylist();
    component.excluirPlaylist();

    const requisicao = httpMock.expectOne(`${apiUrl}/10`);
    requisicao.flush(null, { status: 204, statusText: 'No Content' });

    expect(confirmSpy).toHaveBeenCalledOnce();
  });

  it('não deve remover a música quando o usuário cancela a confirmação', () => {
    vi.spyOn(window, 'confirm').mockReturnValue(false);

    fixture.detectChanges();
    httpMock.expectOne(`${apiUrl}/10`).flush(playlistComUmaMusica());

    component.removerMusica(5);

    httpMock.expectNone(`${apiUrl}/10/musicas/5`);
    expect(component.removendoMusicaId).toBeNull();
  });

  it('deve remover a música da playlist de favoritos quando descurtida', () => {
    fixture.detectChanges();
    httpMock.expectOne(`${apiUrl}/10`).flush(playlistComUmaMusica());

    component.aoAlternarCurtidaNaFavoritos(5, false);

    expect(component.playlist?.musicas).toHaveLength(0);
  });

  it('não deve alterar a lista quando a música permanece curtida', () => {
    fixture.detectChanges();
    httpMock.expectOne(`${apiUrl}/10`).flush(playlistComUmaMusica());

    component.aoAlternarCurtidaNaFavoritos(5, true);

    expect(component.playlist?.musicas).toHaveLength(1);
  });

  it('deve trocar a capa da música por padrão em caso de erro', () => {
    fixture.detectChanges();
    httpMock.expectOne(`${apiUrl}/10`).flush(playlistComUmaMusica());

    const imagem = document.createElement('img');
    component.aoFalharCapa({ target: imagem } as unknown as Event);

    expect(imagem.src).toContain('/capa-padrao.png');
  });

  const cenariosDeErroAoCarregar = [
    { status: 401, mensagemEsperada: 'Sua sessão expirou. Faça login novamente.' },
    { status: 403, mensagemEsperada: 'Você não tem permissão para ver essa playlist.' },
    { status: 404, mensagemEsperada: 'Essa playlist não existe ou foi removida.' }
  ];

  for (const cenario of cenariosDeErroAoCarregar) {
    it(`deve tratar erro ${cenario.status} ao carregar a playlist`, () => {
      vi.spyOn(console, 'error').mockImplementation(() => {});
      const playlistService = TestBed.inject(PlaylistService);

      vi.spyOn(playlistService, 'buscarPorId').mockReturnValue(
        throwError(
          () => new HttpErrorResponse({ status: cenario.status })
        )
      );

      fixture.detectChanges();
      httpMock.expectNone(`${apiUrl}/10`);

      expect(component.mensagemErro).toBe(cenario.mensagemEsperada);
    });
  }

  it('deve informar playlist inválida quando a rota não possui id', async () => {
    TestBed.resetTestingModule();

    await TestBed.configureTestingModule({
      imports: [PlaylistDetalhe],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({}) } }
        }
      ]
    }).compileComponents();

    const fixtureSemId = TestBed.createComponent(PlaylistDetalhe);
    const componentSemId = fixtureSemId.componentInstance;
    const httpMockSemId = TestBed.inject(HttpTestingController);

    fixtureSemId.detectChanges();

    expect(componentSemId.mensagemErro).toBe('Playlist inválida.');
    expect(componentSemId.carregando).toBe(false);
    httpMockSemId.verify();
  });
});
