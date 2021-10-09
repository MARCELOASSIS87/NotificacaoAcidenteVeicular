package com.marceloassis.notificacaoacidenteveicular

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.marceloassis.notificacaoacidenteveicular.databinding.TelaInicialBinding


class TelaInicial : AppCompatActivity() {
    private lateinit var binding: TelaInicialBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.tela_inicial)
    }
    fun chamaTelaInicial(view: View) {
        val intent = Intent(this,MainActivity::class.java).apply {  }
        startActivity(intent)
    }



}