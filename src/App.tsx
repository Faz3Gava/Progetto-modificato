import React, { useState, useMemo } from 'react';
import { 
  FileCode, 
  Download, 
  Copy, 
  Check, 
  Terminal, 
  Folder, 
  Search, 
  Play, 
  CheckCircle2, 
  Layers, 
  BookOpen, 
  Code2,
  Cpu,
  ShieldCheck,
  ChevronRight
} from 'lucide-react';
import JSZip from 'jszip';
import { JAVA_PROJECT_FILES, JavaSourceFile } from './data/javaFiles';

export function App() {
  const [selectedFile, setSelectedFile] = useState<JavaSourceFile>(() => {
    return JAVA_PROJECT_FILES.find(f => f.name === 'CityLogicApp.java') || JAVA_PROJECT_FILES[0];
  });
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [selectedCategory, setSelectedCategory] = useState<string>('all');
  const [copied, setCopied] = useState<boolean>(false);
  const [isZipping, setIsZipping] = useState<boolean>(false);
  const [activeTab, setActiveTab] = useState<'code' | 'instructions' | 'architecture'>('code');

  const categories = [
    { id: 'all', label: 'Tutti i File', count: JAVA_PROJECT_FILES.length },
    { id: 'ui', label: 'JavaFX GUI & FXML', count: JAVA_PROJECT_FILES.filter(f => f.category === 'ui').length },
    { id: 'core', label: 'Domain Core', count: JAVA_PROJECT_FILES.filter(f => f.category === 'core').length },
    { id: 'map', label: 'Map & Grid', count: JAVA_PROJECT_FILES.filter(f => f.category === 'map').length },
    { id: 'buildings', label: 'Buildings', count: JAVA_PROJECT_FILES.filter(f => f.category === 'buildings').length },
    { id: 'tick', label: 'Simulation & Ticks', count: JAVA_PROJECT_FILES.filter(f => f.category === 'tick').length },
    { id: 'application', label: 'Application Facade', count: JAVA_PROJECT_FILES.filter(f => f.category === 'application').length },
    { id: 'policies', label: 'Policies', count: JAVA_PROJECT_FILES.filter(f => f.category === 'policies').length },
    { id: 'test', label: 'JUnit 5 Tests', count: JAVA_PROJECT_FILES.filter(f => f.category === 'test').length },
    { id: 'config', label: 'Maven & Scripts', count: JAVA_PROJECT_FILES.filter(f => f.category === 'config').length },
  ];

  const filteredFiles = useMemo(() => {
    return JAVA_PROJECT_FILES.filter(file => {
      const matchesCategory = selectedCategory === 'all' || file.category === selectedCategory;
      const matchesSearch = file.name.toLowerCase().includes(searchTerm.toLowerCase()) || 
                            file.path.toLowerCase().includes(searchTerm.toLowerCase());
      return matchesCategory && matchesSearch;
    });
  }, [selectedCategory, searchTerm]);

  const handleCopy = () => {
    navigator.clipboard.writeText(selectedFile.code);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleDownloadZip = async () => {
    try {
      setIsZipping(true);
      const zip = new JSZip();

      // Add all project files into standard Maven structure
      JAVA_PROJECT_FILES.forEach(file => {
        zip.file(file.path, file.code);
      });

      const blob = await zip.generateAsync({ type: 'blob' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'citylogic-java-project.zip';
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
    } catch (err) {
      console.error('Failed to create ZIP', err);
    } finally {
      setIsZipping(false);
    }
  };

  const lines = selectedFile.code.split('\n');

  return (
    <div className="flex flex-col h-screen w-screen bg-slate-950 text-slate-100 overflow-hidden font-sans">
      {/* Main Top Header */}
      <header className="bg-slate-900 border-b border-slate-800 px-6 py-3.5 flex items-center justify-between shrink-0">
        <div className="flex items-center gap-3.5">
          <div className="w-10 h-10 rounded-xl bg-orange-500/20 text-orange-400 border border-orange-500/30 flex items-center justify-center font-bold text-lg shadow-sm">
            ☕
          </div>
          <div>
            <div className="flex items-center gap-2.5">
              <h1 className="text-lg font-bold tracking-tight text-white">
                CityLogic — Progetto Java (JavaFX + Maven)
              </h1>
              <span className="px-2.5 py-0.5 rounded-full bg-emerald-950 text-emerald-300 border border-emerald-800/60 text-xs font-semibold flex items-center gap-1">
                <CheckCircle2 className="w-3 h-3 text-emerald-400" /> Java 17 + OpenJFX 21
              </span>
            </div>
            <p className="text-xs text-slate-400">
              Architettura Domain-Driven Design (DDD), pipeline di simulazione a tick transazionali, canvas interattivo e GUI FXML
            </p>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="flex items-center gap-3">
          <div className="hidden lg:flex items-center gap-2 bg-slate-950 px-3 py-1.5 rounded-xl border border-slate-800 text-xs text-slate-300 font-mono">
            <Terminal className="w-3.5 h-3.5 text-emerald-400" />
            <span>mvn clean javafx:run</span>
          </div>

          <button
            onClick={handleDownloadZip}
            disabled={isZipping}
            className="flex items-center gap-2 px-4 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white font-semibold text-xs transition-all shadow-md shadow-emerald-900/30 cursor-pointer disabled:opacity-50"
            title="Scarica il progetto Maven completo pronto per l'esecuzione in locale"
          >
            <Download className="w-4 h-4" />
            <span>{isZipping ? 'Creazione ZIP...' : 'Scarica Progetto Java (.zip)'}</span>
          </button>
        </div>
      </header>

      {/* Navigation Subbar */}
      <div className="bg-slate-900/60 border-b border-slate-800/80 px-6 py-2 flex items-center justify-between text-xs shrink-0">
        <div className="flex items-center gap-2">
          <button
            onClick={() => setActiveTab('code')}
            className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg font-medium transition-colors cursor-pointer ${
              activeTab === 'code' ? 'bg-indigo-600 text-white' : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800'
            }`}
          >
            <Code2 className="w-3.5 h-3.5" /> Esplora Codice ({JAVA_PROJECT_FILES.length} file)
          </button>
          <button
            onClick={() => setActiveTab('instructions')}
            className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg font-medium transition-colors cursor-pointer ${
              activeTab === 'instructions' ? 'bg-indigo-600 text-white' : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800'
            }`}
          >
            <BookOpen className="w-3.5 h-3.5" /> Guida all'Avvio Locale
          </button>
          <button
            onClick={() => setActiveTab('architecture')}
            className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg font-medium transition-colors cursor-pointer ${
              activeTab === 'architecture' ? 'bg-indigo-600 text-white' : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800'
            }`}
          >
            <Layers className="w-3.5 h-3.5" /> Struttura &amp; Invarianti
          </button>
        </div>

        <div className="text-[11px] text-slate-400">
          <span>Solo codice sorgente Java puro (Maven layout)</span>
        </div>
      </div>

      {/* Main Content Area */}
      {activeTab === 'code' && (
        <div className="flex-1 flex overflow-hidden">
          {/* Left Sidebar: File Browser */}
          <aside className="w-80 bg-slate-950 border-r border-slate-800 flex flex-col shrink-0">
            {/* Search Input */}
            <div className="p-3 border-b border-slate-800">
              <div className="relative">
                <Search className="w-3.5 h-3.5 text-slate-500 absolute left-3 top-2.5" />
                <input
                  type="text"
                  placeholder="Cerca classi, interfacce, risorse..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="w-full bg-slate-900 border border-slate-800 rounded-lg pl-8 pr-3 py-1.5 text-xs text-slate-200 placeholder-slate-500 focus:outline-none focus:border-indigo-500"
                />
              </div>
            </div>

            {/* Category Filter Pills */}
            <div className="px-3 py-2 border-b border-slate-800 flex flex-wrap gap-1">
              {categories.map(cat => (
                <button
                  key={cat.id}
                  onClick={() => setSelectedCategory(cat.id)}
                  className={`px-2 py-0.5 rounded text-[10px] font-medium transition-colors cursor-pointer ${
                    selectedCategory === cat.id
                      ? 'bg-indigo-600 text-white'
                      : 'bg-slate-900 text-slate-400 hover:bg-slate-800 hover:text-slate-200'
                  }`}
                >
                  {cat.label} ({cat.count})
                </button>
              ))}
            </div>

            {/* File List */}
            <div className="flex-1 overflow-y-auto p-2 space-y-0.5">
              {filteredFiles.map(file => {
                const isSelected = selectedFile.path === file.path;
                return (
                  <button
                    key={file.path}
                    onClick={() => setSelectedFile(file)}
                    className={`w-full text-left px-2.5 py-1.5 rounded-lg flex items-center justify-between text-xs transition-colors cursor-pointer ${
                      isSelected
                        ? 'bg-indigo-600/20 text-indigo-300 border border-indigo-500/40 font-medium'
                        : 'text-slate-400 hover:text-slate-200 hover:bg-slate-900 border border-transparent'
                    }`}
                  >
                    <div className="flex items-center gap-2 min-w-0">
                      <FileCode className={`w-3.5 h-3.5 shrink-0 ${
                        file.language === 'java' ? 'text-orange-400' : 
                        file.language === 'xml' ? 'text-cyan-400' : 'text-slate-400'
                      }`} />
                      <span className="truncate font-mono">{file.name}</span>
                    </div>
                    <span className="text-[9px] uppercase px-1 rounded bg-slate-900 text-slate-500 shrink-0 ml-1">
                      {file.category}
                    </span>
                  </button>
                );
              })}
              {filteredFiles.length === 0 && (
                <div className="p-4 text-center text-xs text-slate-500">
                  Nessun file trovato per "{searchTerm}"
                </div>
              )}
            </div>

            {/* Bottom Quick Stats */}
            <div className="p-3 bg-slate-900/50 border-t border-slate-800 text-[11px] text-slate-400">
              <div className="flex items-center justify-between">
                <span>File Progetto:</span>
                <span className="font-mono text-slate-200">{JAVA_PROJECT_FILES.length} sorgenti</span>
              </div>
            </div>
          </aside>

          {/* Right Editor: Code Viewer */}
          <main className="flex-1 flex flex-col bg-slate-950 overflow-hidden">
            {/* File Info & Action Bar */}
            <div className="px-5 py-2.5 bg-slate-900/70 border-b border-slate-800 flex items-center justify-between shrink-0">
              <div className="flex items-center gap-2 text-xs font-mono">
                <Folder className="w-3.5 h-3.5 text-indigo-400" />
                <span className="text-slate-400">{selectedFile.path}</span>
                <span className="text-slate-600">|</span>
                <span className="text-slate-400">{lines.length} linee</span>
              </div>

              <button
                onClick={handleCopy}
                className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-medium transition-colors cursor-pointer border border-slate-700"
              >
                {copied ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
                <span>{copied ? 'Copiato!' : 'Copia Codice'}</span>
              </button>
            </div>

            {/* Code Content with Line Numbers */}
            <div className="flex-1 overflow-auto p-4 font-mono text-[12.5px] leading-relaxed text-slate-300 selection:bg-indigo-500/30">
              <div className="flex">
                <div className="select-none text-right pr-4 text-slate-600 font-mono text-[11.5px] border-r border-slate-800/80 mr-4">
                  {lines.map((_, i) => (
                    <div key={i}>{i + 1}</div>
                  ))}
                </div>
                <pre className="flex-1 whitespace-pre overflow-x-auto text-slate-200">
                  {selectedFile.code}
                </pre>
              </div>
            </div>
          </main>
        </div>
      )}

      {/* Instructions Tab */}
      {activeTab === 'instructions' && (
        <div className="flex-1 overflow-y-auto p-8 max-w-4xl mx-auto space-y-6">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 space-y-4">
            <h2 className="text-xl font-bold text-white flex items-center gap-2">
              <Terminal className="w-5 h-5 text-emerald-400" /> Come Eseguire il Progetto in Locale
            </h2>
            <p className="text-sm text-slate-300">
              Il progetto è un'applicazione desktop standalone configurata con <strong>Maven</strong>, <strong>Java 17</strong> e <strong>JavaFX 21</strong>.
            </p>

            <div className="space-y-3">
              <h3 className="text-sm font-semibold text-white uppercase tracking-wider text-xs">Requisiti minimi:</h3>
              <ul className="text-xs text-slate-300 list-disc list-inside space-y-1">
                <li>JDK 17 o 21 (ad es. Eclipse Temurin, Liberica JDK o Oracle JDK)</li>
                <li>Apache Maven 3.8+ (oppure il Maven integrato nel tuo IDE)</li>
              </ul>
            </div>

            <div className="space-y-2 pt-2">
              <h3 className="text-sm font-semibold text-white">1. Avvio rapido con Maven:</h3>
              <div className="bg-slate-950 p-3 rounded-xl border border-slate-800 font-mono text-xs text-emerald-400 flex items-center justify-between">
                <span>mvn clean javafx:run</span>
                <button
                  onClick={() => navigator.clipboard.writeText('mvn clean javafx:run')}
                  className="px-2 py-1 rounded bg-slate-800 text-slate-300 hover:text-white text-[11px]"
                >
                  Copia
                </button>
              </div>
            </div>

            <div className="space-y-2">
              <h3 className="text-sm font-semibold text-white">2. Esecuzione dei Test Unitari (JUnit 5):</h3>
              <div className="bg-slate-950 p-3 rounded-xl border border-slate-800 font-mono text-xs text-emerald-400 flex items-center justify-between">
                <span>mvn test</span>
                <button
                  onClick={() => navigator.clipboard.writeText('mvn test')}
                  className="px-2 py-1 rounded bg-slate-800 text-slate-300 hover:text-white text-[11px]"
                >
                  Copia
                </button>
              </div>
            </div>

            <div className="space-y-2">
              <h3 className="text-sm font-semibold text-white">3. Creazione del pacchetto JAR eseguibile:</h3>
              <div className="bg-slate-950 p-3 rounded-xl border border-slate-800 font-mono text-xs text-emerald-400 flex items-center justify-between">
                <span>mvn clean package && java -jar target/citylogic-javafx-1.0.0.jar</span>
                <button
                  onClick={() => navigator.clipboard.writeText('mvn clean package && java -jar target/citylogic-javafx-1.0.0.jar')}
                  className="px-2 py-1 rounded bg-slate-800 text-slate-300 hover:text-white text-[11px]"
                >
                  Copia
                </button>
              </div>
            </div>
          </div>

          <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 space-y-4">
            <h2 className="text-lg font-bold text-white">Importazione negli IDE</h2>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-xs">
              <div className="p-4 rounded-xl bg-slate-950 border border-slate-800 space-y-2">
                <span className="font-bold text-indigo-400">IntelliJ IDEA</span>
                <p className="text-slate-400">
                  Apri la cartella del progetto contenente il <code className="text-slate-200">pom.xml</code>. IntelliJ configurerà Maven automaticamente. Esegui la classe <code className="text-slate-200">Main.java</code>.
                </p>
              </div>
              <div className="p-4 rounded-xl bg-slate-950 border border-slate-800 space-y-2">
                <span className="font-bold text-cyan-400">Eclipse</span>
                <p className="text-slate-400">
                  Seleziona <em>File → Import → Existing Maven Projects</em>, scegli la cartella del progetto e clicca <em>Finish</em>. Avvia con <em>Run As → Java Application</em>.
                </p>
              </div>
              <div className="p-4 rounded-xl bg-slate-950 border border-slate-800 space-y-2">
                <span className="font-bold text-emerald-400">VS Code</span>
                <p className="text-slate-400">
                  Installa il <em>Java Extension Pack</em>. Apri la cartella del progetto ed esegui il target Maven <code className="text-slate-200">javafx:run</code> o <code className="text-slate-200">Main.java</code>.
                </p>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Architecture Tab */}
      {activeTab === 'architecture' && (
        <div className="flex-1 overflow-y-auto p-8 max-w-4xl mx-auto space-y-6">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 space-y-4">
            <h2 className="text-xl font-bold text-white flex items-center gap-2">
              <ShieldCheck className="w-5 h-5 text-indigo-400" /> Architettura Domain-Driven Design (DDD)
            </h2>
            <p className="text-sm text-slate-300 leading-relaxed">
              Il dominio è rigorosamente isolato e suddiviso secondo i principi di Domain-Driven Design e Clean Architecture:
            </p>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-xs pt-2">
              <div className="p-4 rounded-xl bg-slate-950 border border-slate-800 space-y-2">
                <h4 className="font-semibold text-indigo-400 flex items-center gap-1.5">
                  <Cpu className="w-3.5 h-3.5" /> com.citylogic.domain.core
                </h4>
                <p className="text-slate-400">
                  <strong>CityAggregate:</strong> Aggregate Root che valida gli invarianti (bancarotta a -$10.000, popolazione ≥ 0, felicità tra 0% e 100%).<br/>
                  <strong>CitySnapshot:</strong> DTO immutabile per salvare lo stato prima di ogni tick e consentire il rollback in caso di errore.<br/>
                  <strong>ResourceDelta:</strong> Value Object immutabile che accumula i delta di bilancio, inquinamento, cittadini e felicità.
                </p>
              </div>

              <div className="p-4 rounded-xl bg-slate-950 border border-slate-800 space-y-2">
                <h4 className="font-semibold text-emerald-400 flex items-center gap-1.5">
                  <Layers className="w-3.5 h-3.5" /> com.citylogic.domain.tick
                </h4>
                <p className="text-slate-400">
                  <strong>SimulationEngine:</strong> Motore transazionale che esegue la pipeline di tick (<code className="text-slate-300">ProductionPhase</code>, <code className="text-slate-300">PolicyEvaluationPhase</code>). In caso di violazione di invarianti, applica immediatamente il rollback automatico.
                </p>
              </div>

              <div className="p-4 rounded-xl bg-slate-950 border border-slate-800 space-y-2">
                <h4 className="font-semibold text-cyan-400 flex items-center gap-1.5">
                  <Folder className="w-3.5 h-3.5" /> com.citylogic.domain.map
                </h4>
                <p className="text-slate-400">
                  <strong>Grid:</strong> Matrice 2D che implementa <code className="text-slate-300">IGridReadPort</code> (lettura) e <code className="text-slate-300">IGridCommandPort</code> (comandi di costruzione/demolizione). Supporta footprint multi-tile (es. 2x2 per le fabbriche) e metodi <code className="text-slate-300">isOccupied</code> / <code className="text-slate-300">GetBuilding</code>.
                </p>
              </div>

              <div className="p-4 rounded-xl bg-slate-950 border border-slate-800 space-y-2">
                <h4 className="font-semibold text-orange-400 flex items-center gap-1.5">
                  <FileCode className="w-3.5 h-3.5" /> com.citylogic.ui (JavaFX)
                </h4>
                <p className="text-slate-400">
                  <strong>CityLogicApp:</strong> Entrypoint JavaFX Application con caricamento FXML.<br/>
                  <strong>CityMapCanvas:</strong> Canvas interattivo 2D con rendering di tile, edifici con sfumature per categoria, ombre di posizionamento ed effetti di selezione.<br/>
                  <strong>GameViewController:</strong> Controller con binding dei KPI, toolbar strumenti e timeline di animazione.
                </p>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default App;
