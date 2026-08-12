import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';

import { Catalogo } from './catalogo';
import { MusicaResponse } from '../../models/MusicaResponse';

describe('Catalogo', () => {
  let component: Catalogo;
  let fixture: ComponentFixture<Catalogo>;
  let httpMock: HttpTestingController;

  const musicasUrl = 'http://localhost:8080/api/musicas';
  const playlistsUrl = 'http://localhost:8080/api/playlists';

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Catalogo],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: { paramMap: convertToParamMap({ id: '1' }) },
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Catalogo);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  function musicaDeExemplo(): MusicaResponse {
    return {
      id: 5,
      titulo: 'Bohemian Rhapsody',
      duracaoSegundos: 354,
      anoLancamento: 1975,
      artistaPrincipal: { id: 1, nome: 'Queen' },
      album: { id: 1, titulo: 'A Night at the Opera' },
      generos: [{ id: 1, nome: 'Rock' }],
    };
  }

  it('deve carregar o catálogo de músicas usando o id da playlist da rota', () => {
    fixture.detectChanges();

    httpMock.expectOne(musicasUrl).flush([musicaDeExemplo()]);

    expect(component.playlistId).toBe(1);
    expect(component.musicas()).toHaveLength(1);
  });

  it('deve adicionar a música na playlist e marcar como adicionada', () => {
    fixture.detectChanges();
    httpMock.expectOne(musicasUrl).flush([musicaDeExemplo()]);

    component.adicionarMusica(5);

    httpMock
      .expectOne(`${playlistsUrl}/1/musicas/5`)
      .flush(null, { status: 204, statusText: 'No Content' });

    expect(component.musicasAdicionadas()[5]).toBe(true);
    expect(component.loadingAdicionar()[5]).toBeFalsy();
    expect(component.mensagemSucesso()).toBeTruthy();
  });

  it('não deve enviar uma segunda requisição para uma música já adicionada', () => {
    fixture.detectChanges();
    httpMock.expectOne(musicasUrl).flush([musicaDeExemplo()]);

    component.adicionarMusica(5);
    httpMock
      .expectOne(`${playlistsUrl}/1/musicas/5`)
      .flush(null, { status: 204, statusText: 'No Content' });

    component.adicionarMusica(5);

    httpMock.expectNone(`${playlistsUrl}/1/musicas/5`);
  });

  it('deve mostrar mensagem de erro e liberar o botão quando a requisição falha', () => {
    fixture.detectChanges();
    httpMock.expectOne(musicasUrl).flush([musicaDeExemplo()]);

    component.adicionarMusica(5);

    httpMock
      .expectOne(`${playlistsUrl}/1/musicas/5`)
      .flush({ message: 'erro' }, { status: 500, statusText: 'Internal Server Error' });

    expect(component.mensagemErro()).toBeTruthy();
    expect(component.loadingAdicionar()[5]).toBeFalsy();
    expect(component.musicasAdicionadas()[5]).toBeFalsy();
  });
});
