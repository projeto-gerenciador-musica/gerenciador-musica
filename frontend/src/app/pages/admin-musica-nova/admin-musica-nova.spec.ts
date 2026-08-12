import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';

import { AdminMusicaNova } from './admin-musica-nova';

describe('AdminMusicaNova', () => {
  let component: AdminMusicaNova;
  let fixture: ComponentFixture<AdminMusicaNova>;
  let httpMock: HttpTestingController;
  let router: Router;

  const apiUrl = 'http://localhost:8080/api/admin/musicas';

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminMusicaNova],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminMusicaNova);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('não deve enviar requisição quando o formulário é inválido', () => {
    component.salvar();

    httpMock.expectNone(apiUrl);
  });

  function preencherFormularioValido(): void {
    component.formularioMusica.setValue({
      titulo: 'Bohemian Rhapsody',
      duracao: '354',
      genero: 'Rock',
      anoLancamento: '1975',
      artista: 'Queen',
      album: 'A Night at the Opera',
    });
  }

  it('deve montar o payload aninhado esperado pelo backend', () => {
    preencherFormularioValido();

    component.salvar();

    const requisicao = httpMock.expectOne(apiUrl);

    expect(requisicao.request.method).toBe('POST');
    expect(requisicao.request.body).toEqual({
      titulo: 'Bohemian Rhapsody',
      duracaoSegundos: 354,
      anoLancamento: 1975,
      artistaPrincipal: { nome: 'Queen' },
      artistasParticipantes: [],
      album: { titulo: 'A Night at the Opera', anoLancamento: 1975 },
      generos: ['Rock'],
    });

    requisicao.flush({ id: 1 });
  });

  it('deve navegar para a listagem quando o cadastro tem sucesso', () => {
    const navigateSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);

    preencherFormularioValido();
    component.salvar();

    httpMock.expectOne(apiUrl).flush({ id: 1 });

    expect(component.mensagemSucesso()).toBeTruthy();
    expect(component.carregando()).toBe(false);
    expect(navigateSpy).toHaveBeenCalledWith(['/admin/banco/musicas']);
  });

  it('deve mostrar mensagem de conflito quando a música já está cadastrada', () => {
    preencherFormularioValido();
    component.salvar();

    httpMock
      .expectOne(apiUrl)
      .flush({ message: 'A música já está cadastrada.' }, { status: 409, statusText: 'Conflict' });

    expect(component.mensagemErro()).toContain('409');
    expect(component.carregando()).toBe(false);
  });
});
