import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { Playlists } from './playlists';
import { PlaylistResponse } from '../../models/PlaylistResponse';

describe('Playlists', () => {
  let component: Playlists;
  let fixture: ComponentFixture<Playlists>;
  let httpMock: HttpTestingController;

  const apiUrl = 'http://localhost:8080/api/playlists';

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Playlists],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Playlists);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('deve carregar as playlists do usuário ao iniciar', () => {
    // A primeira chamada de detectChanges() dispara o ngOnInit.
    fixture.detectChanges();

    httpMock.expectOne(apiUrl).flush([
      { id: 1, nome: 'Favoritas', descricao: '', musicas: [] },
    ] as PlaylistResponse[]);

    expect(component.playlists).toHaveLength(1);
    expect(component.playlists[0].nome).toBe('Favoritas');
    expect(component.carregando).toBe(false);
  });

  it('deve mostrar mensagem de erro quando a busca falha', () => {
    fixture.detectChanges();

    httpMock
      .expectOne(apiUrl)
      .flush({ message: 'erro' }, { status: 500, statusText: 'Internal Server Error' });

    expect(component.mensagemErro).toBeTruthy();
    expect(component.carregando).toBe(false);
  });
});
