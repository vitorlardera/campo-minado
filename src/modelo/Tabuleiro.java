package modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Tabuleiro implements CampoObservador{
	
	private final int linhas;
	private final int colunas;
	private final int minas;
	
	private final List<Campo> campos = new ArrayList<>();
	private final List<Consumer<Boolean>> observadores = new ArrayList<>();

	public Tabuleiro(int linhas, int colunas, int minas) {
		this.linhas = linhas;
		this.colunas = colunas;
		this.minas = minas;
		
		gerarCampos();
		associarOsVizinhos();
		sortearMinas();
		
	}
	
	public void paraCada(Consumer<Campo> funcao) {
		campos.forEach(funcao);
	}
	
	public int getLinhas() {
		return linhas;
	}


	public int getColunas() {
		return colunas;
	}


	public void registrarObservador(Consumer<Boolean> observador) {
		observadores.add(observador);
	}
	
	private void notificarObservadores(boolean resultado) {
		observadores.stream()
		.forEach(o -> o.accept(resultado));
	}
	
	public void abrir(int linha, int coluna) {
		try {
			campos.parallelStream()
			.filter(c -> c.getLinha() == linha && c.getColuna() == coluna)
			.findFirst()
			.ifPresent(c -> c.abrir());

		}catch(Exception e) {
			// FIXME Ajustar a implementação do método abrir
			campos.forEach(c -> c.setAberto(true));
			throw e;
		}
	}
	
	private void mostrarMinas(){
		campos.stream()
		.filter(c -> c.isMinado())
		.filter(c -> !c.isMarcado())
		.forEach(c -> c.setAberto(true));
	}
	
	public void alterarMarcacao(int linha, int coluna) {
		campos.parallelStream()
		.filter(c -> c.getLinha() == linha && c.getColuna() == coluna)
		.findFirst()
		.ifPresent(c -> c.alternarMarcacao());
		
	}


	private void gerarCampos() {
		for(int l = 0; l < linhas; l++) {
			for(int c = 0; c < colunas; c++) {
				Campo campo = new Campo(l, c);
				campo.registrarObservador(this);
				campos.add(campo);
			}
		}
		
	}

	private void associarOsVizinhos() {
		for(Campo c1: campos) {
			for (Campo c2: campos) {
				c1.adicionarVizinho(c2);
			}
		}
		
	}
	
	private void sortearMinas() {
		long minasArmadas = 0;
		Predicate<Campo> minado = c -> c.isMinado();
		
		do {
			int aleatorio = (int) (Math.random() * campos.size());
			campos.get(aleatorio).minar();
			minasArmadas = campos.stream().filter(minado).count();
			
		}while (minasArmadas < minas); 
	}
	public boolean objetivoAlcancado() {
		return campos.stream().allMatch(c -> c.objetivoAlcancado());
}
	
	public void reinicar() {
		campos.stream().forEach(c -> c.reiniciar());
		sortearMinas();
	
	}
		
	@Override
	public void eventoOcorreu(Campo campo, CampoEvento evento) {
		if(evento == CampoEvento.EXPLODIR) {
			System.out.println("Você perdeu");
			mostrarMinas();
			notificarObservadores(false);
		}else if (objetivoAlcancado()) {
			System.out.println("Você ganhou");
			notificarObservadores(true);
		}
		
	}

}
