import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';

import { PlaylistDetalhe } from './playlist-detalhe';
import { PlaylistResponse } from '../../models/PlaylistResponse';

describe('PlaylistDetalhe', () => {
  let component: PlaylistDetalhe;
  let fixture: ComponentFixture<PlaylistDetalhe>;
  let httpMock: HttpTestingController;

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
  });

  afterEach(() => {
    httpMock.verify();
  });

  function playlistComUmaMusica(): PlaylistResponse {
    return {
      id: 10,
      nome: 'Favoritas',
      descricao: '',
      musicas: [{ id: 5, titulo: 'Bohemian Rhapsody', artista: 'Queen' }],
    };
  }

  it('deve carregar a playlist ao iniciar', () => {
    fixture.detectChanges();

    httpMock.expectOne(`${apiUrl}/10`).flush(playlistComUmaMusica());

    expect(component.playlist?.nome).toBe('Favoritas');
    expect(component.carregando).toBe(false);
  });

  it('deve remover a música da lista local quando a remoção tem sucesso', () => {
    fixture.detectChanges();
    httpMock.expectOne(`${apiUrl}/10`).flush(playlistComUmaMusica());

    component.removerMusica(5);

    httpMock
      .expectOne(`${apiUrl}/10/musicas/5`)
      .flush(null, { status: 204, statusText: 'No Content' });

    expect(component.playlist?.musicas).toHaveLength(0);
    expect(component.removendoMusicaId).toBeNull();
  });

  it('não deve permitir clicar em remover duas vezes para a mesma música', () => {
    fixture.detectChanges();
    httpMock.expectOne(`${apiUrl}/10`).flush(playlistComUmaMusica());

    component.removerMusica(5);
    component.removerMusica(5);

    // Se o segundo clique tivesse dobrado a requisição, expectOne
    // falharia por encontrar duas pendentes em vez de uma.
    httpMock
      .expectOne(`${apiUrl}/10/musicas/5`)
      .flush(null, { status: 204, statusText: 'No Content' });
  });

  it('deve mostrar mensagem de erro quando a remoção falha', () => {
    fixture.detectChanges();
    httpMock.expectOne(`${apiUrl}/10`).flush(playlistComUmaMusica());

    component.removerMusica(5);

    httpMock
      .expectOne(`${apiUrl}/10/musicas/5`)
      .flush({ message: 'erro' }, { status: 500, statusText: 'Internal Server Error' });

    expect(component.mensagemErro).toBeTruthy();
    expect(component.playlist?.musicas).toHaveLength(1);
  });
});
