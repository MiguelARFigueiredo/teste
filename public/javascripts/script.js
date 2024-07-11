document.addEventListener('DOMContentLoaded', function() {
    // Botões e conteúdos do primeiro conjunto
    const mybutton1 = document.getElementById('mybuttonid');
    const mycontent1 = document.getElementById('mybox1id');

    mybutton1.addEventListener('click', function() {
        if (mycontent1.style.display === 'none' || mycontent1.style.display === '') {
            mycontent1.style.display = 'block';
            mybutton1.textContent = 'Read Less';
        } else {
            mycontent1.style.display = 'none';
            mybutton1.textContent = 'Read More';
        }
    });

    // Botões e conteúdos do segundo conjunto
    const mybutton2 = document.getElementById('mybuttonid2');
    const mycontent2 = document.getElementById('mybox2id');

    mybutton2.addEventListener('click', function() {
        if (mycontent2.style.display === 'none' || mycontent2.style.display === '') {
            mycontent2.style.display = 'block';
            mybutton2.textContent = 'Read Less';
        } else {
            mycontent2.style.display = 'none';
            mybutton2.textContent = 'Read More';
        }
    });
});

