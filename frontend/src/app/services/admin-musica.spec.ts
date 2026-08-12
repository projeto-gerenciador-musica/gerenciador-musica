import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';

import { AdminMusicaService } from './admin-musica';
import { MusicaResponse } from '../models/MusicaResponse';

describe('AdminMusicaService', () => {
  let service: AdminMusicaService;
  let httpMock: HttpTestingController;

  const apiUrl = 'http://localhost:8080/api/musicas';

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AdminMusicaService,
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });

    service = TestBed.inject(AdminMusicaService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('deve buscar a lista de músicas cadastradas', () => {
    service.listarMusicas().subscribe((musicas) => {
      expect(musicas).toHaveLength(1);
      expect(musicas[0].titulo).toBe('Bohemian Rhapsody');
    });

    const requisicao = httpMock.expectOne(apiUrl);

    expect(requisicao.request.method).toBe('GET');

    requisicao.flush([
      {
        id: 1,
        titulo: 'Bohemian Rhapsody',
        duracaoSegundos: 354,
        anoLancamento: 1975,
        artistaPrincipal: { id: 1, nome: 'Queen' },
        album: { id: 1, titulo: 'A Night at the Opera' },
        generos: [{ id: 1, nome: 'Rock' }],
      },
    ] as MusicaResponse[]);
  });
});
