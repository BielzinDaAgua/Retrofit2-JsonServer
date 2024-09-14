package com.example.navegacao1.ui.telas

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.navegacao1.model.dados.RetrofitClient
import com.example.navegacao1.model.dados.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun TelaPrincipal(modifier: Modifier = Modifier, onLogoffClick: () -> Unit) {
    val scope = rememberCoroutineScope()
    var usuarios by remember { mutableStateOf<List<Usuario>>(emptyList()) }
    var novoNome by remember { mutableStateOf(TextFieldValue("")) }
    var novaSenha by remember { mutableStateOf(TextFieldValue("")) }
    var usuarioRemover by remember { mutableStateOf(TextFieldValue("")) }
    var erro by remember { mutableStateOf("") } // Estado para mensagens de erro

    LaunchedEffect(Unit) {
        scope.launch {
            usuarios = getUsuarios { erroMessage ->
                erro = erroMessage // Atualiza a mensagem de erro
            }
        }
    }

    Column(modifier = modifier.padding(16.dp)) {
        Text(text = "Tela Principal", style = MaterialTheme.typography.headlineSmall)

        // Exibir mensagem de erro, se houver
        if (erro.isNotEmpty()) {
            Text(
                text = "Erro: $erro",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(8.dp)
            )
        }

        // Exibir lista de usuários
        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
            items(usuarios) { usuario ->
                Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text(text = "Nome: ${usuario.nome}")
                        Text(text = "ID: ${usuario.id}")
                    }
                }
            }
        }

        // Campo para inserção de novo usuário
        OutlinedTextField(
            value = novoNome,
            onValueChange = { novoNome = it },
            label = { Text("Nome do novo usuário") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )
        OutlinedTextField(
            value = novaSenha,
            onValueChange = { novaSenha = it },
            label = { Text("Senha do novo usuário") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )
        Button(onClick = {
            scope.launch {
                inserirUsuario(
                    Usuario(id = "", nome = novoNome.text, senha = novaSenha.text)
                )
                usuarios = getUsuarios { erroMessage ->
                    erro = erroMessage // Atualiza a mensagem de erro
                }
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Inserir Usuário")
        }

        // Campo para remover usuário
        OutlinedTextField(
            value = usuarioRemover,
            onValueChange = { usuarioRemover = it },
            label = { Text("ID do usuário para remover") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )
        Button(onClick = {
            scope.launch {
                removerUsuario(usuarioRemover.text) { erroMessage ->
                    erro = erroMessage // Atualiza a mensagem de erro
                }
                usuarios = getUsuarios { erroMessage ->
                    erro = erroMessage // Atualiza a mensagem de erro
                }
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Remover Usuário")
        }

        // Botão de logoff
        Button(onClick = onLogoffClick, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            Text("Sair")
        }
    }
}

// Função para buscar usuários com captura de erros
suspend fun getUsuarios(onError: (String) -> Unit): List<Usuario> {
    return withContext(Dispatchers.IO) {
        try {
            RetrofitClient.usuarioService.listar()
        } catch (e: Exception) {
            Log.e("TelaPrincipal", "Erro ao carregar usuários: ${e.message}")
            onError("Erro ao carregar usuários: ${e.message}")
            emptyList()
        }
    }
}

suspend fun inserirUsuario(usuario: Usuario) {
    withContext(Dispatchers.IO) {
        try {
            // Primeiro, buscar todos os usuários para determinar o próximo ID
            val usuariosExistentes = RetrofitClient.usuarioService.listar()

            // Verificar qual é o maior ID atual e adicionar 1
            val proximoId = if (usuariosExistentes.isNotEmpty()) {
                usuariosExistentes.maxOf { it.id.toIntOrNull() ?: 0 } + 1
            } else {
                1
            }

            // Atribuir o novo ID ao usuário
            usuario.id = proximoId.toString()

            // Inserir o usuário no servidor
            RetrofitClient.usuarioService.inserir(usuario)
        } catch (e: Exception) {
            Log.e("TelaPrincipal", "Erro ao inserir usuário: ${e.message}")
        }
    }
}


// Função para remover usuário com captura de erros
suspend fun removerUsuario(id: String, onError: (String) -> Unit) {
    withContext(Dispatchers.IO) {
        try {
            RetrofitClient.usuarioService.remover(id)
        } catch (e: Exception) {
            Log.e("TelaPrincipal", "Erro ao remover usuário: ${e.message}")
            onError("Erro ao remover usuário: ${e.message}")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTelaPrincipal() {
    TelaPrincipal(onLogoffClick = {})
}
